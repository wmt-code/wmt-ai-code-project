package com.wmt.wmtaicode.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wmt.wmtaicode.ai.model.message.*;
import com.wmt.wmtaicode.ai.tools.BaseTool;
import com.wmt.wmtaicode.ai.tools.ToolManager;
import com.wmt.wmtaicode.constant.AppConstant;
import com.wmt.wmtaicode.core.builder.VueProjectBuilder;
import com.wmt.wmtaicode.model.dto.chathistory.AddChatHistoryReq;
import com.wmt.wmtaicode.model.enums.MessageTypeEnum;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * Json消息流处理器
 * 处理VUE项目的代码生成的流式消息,包含工具的调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {
	@Resource
	private VueProjectBuilder vueProjectBuilder;
	@Resource
	private ToolManager toolManager;

	/**
	 * 处理流式消息块
	 *
	 * @param originFlux         原始流
	 * @param chatHistoryService 聊天记录服务
	 * @param appId              应用ID
	 * @param loginUser          登录用户
	 * @return 处理后的流
	 */
	public Flux<String> handle(Flux<String> originFlux,
							   ChatHistoryService chatHistoryService,
							   long appId, UserVO loginUser) {
		StringBuilder aiResponseBuilder = new StringBuilder();
		// 确保同一工具调用只出现一次
		Set<String> seenToolIds = new HashSet<>();
		return originFlux.map(chunk -> {
					// 解析每个消息块 ai响应内容、工具请求、工具调用结果
					return handleJsonMessageChunk(chunk, aiResponseBuilder, seenToolIds);
				}).filter(StrUtil::isNotEmpty)// 过滤空字符串
				.doOnComplete(() -> {
					// 添加聊天记录
					AddChatHistoryReq addChatHistoryReq = AddChatHistoryReq.builder()
							.appId(appId)
							.messageType(MessageTypeEnum.AI.getValue())
							.message(aiResponseBuilder.toString())
							.build();
					chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
					// 执行完后，进行vue项目的构建和打包
					String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
					vueProjectBuilder.buildProjectAsync(projectPath);
				}).doOnError(error -> {
					// 记录异常日志
					AddChatHistoryReq addChatHistoryReq = AddChatHistoryReq.builder()
							.appId(appId)
							.messageType(MessageTypeEnum.AI.getValue())
							.message("AI响应异常：" + error.getMessage())
							.build();
					chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
				});
	}

	/**
	 * 解析并收集TokenStream中的Json消息块
	 */
	private String handleJsonMessageChunk(String chunk, StringBuilder aiResponseBuilder, Set<String> seenToolIds) {
		// 解析Json消息,获取对应的消息类型，根据消息类型进行不同的处理
		StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
		String streamMessageType = streamMessage.getType();
		StreamMessageTypeEnum value = StreamMessageTypeEnum.getEnumByValue(streamMessageType);
		switch (value) {
			case AI_RESPONSE -> {
				AiResponseMessage aiResponseMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
				String data = aiResponseMessage.getData();
				// 拼接响应
				aiResponseBuilder.append(data);
				return data;
			}
			case TOOL_REQUEST -> {
				ToolExecutedRequestMessage toolExecutedRequestMessage = JSONUtil.toBean(chunk,
						ToolExecutedRequestMessage.class);
				String toolId = toolExecutedRequestMessage.getId();
				// 获取工具名称
				String toolName = toolExecutedRequestMessage.getName();
				BaseTool tool = toolManager.getToolByName(toolName);
				// 检查是否第一次调用工具
				if (toolId != null && !seenToolIds.contains(toolId)) {
					// 第一次调用则记录ID
					seenToolIds.add(toolId);
					return tool.generateToolRequestResponse();
				} else {
					// 不是则返回空字符串
					return "";
				}
			}
			case TOOL_EXECUTED -> {
				ToolExecutedResultMessage toolExecutedResultMessage = JSONUtil.toBean(chunk,
						ToolExecutedResultMessage.class);
				JSONObject jsonObject = JSONUtil.parseObj(toolExecutedResultMessage.getArguments());
				String toolName = toolExecutedResultMessage.getName();
				BaseTool tool = toolManager.getToolByName(toolName);
				String result = tool.generateToolExecutedResult(jsonObject);
				String output = String.format("\n\n%s\n\n", result);
				// 拼接响应
				aiResponseBuilder.append(output);
				return output;
			}
			default -> {
				log.error("未知的流式消息类型: {}", streamMessageType);
				return "";
			}
		}
	}
}
