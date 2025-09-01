package com.wmt.wmtaicode.core.handler;

import com.wmt.wmtaicode.model.dto.chathistory.AddChatHistoryReq;
import com.wmt.wmtaicode.model.enums.MessageTypeEnum;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.ChatHistoryService;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理HTML和Muti_File两种简单文本流
 */
public class SimpleTextStreamHandler {
	/**
	 * 处理流，收集AI响应内容，并在流结束后保存聊天记录
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
		return originFlux
				.map(chunk -> {
					// 收集ai响应内容
					aiResponseBuilder.append(chunk);
					return chunk;
				}).doOnComplete(() -> {
					// 保存聊天记录
					AddChatHistoryReq addChatHistoryReq = AddChatHistoryReq.builder()
							.appId(appId)
							.messageType(MessageTypeEnum.AI.getValue())
							.message(aiResponseBuilder.toString())
							.build();
					chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
				}).doOnError(error -> {
					String errorMessage = "AI响应异常：" + error.getMessage();
					// 保存聊天记录
					AddChatHistoryReq addChatHistoryReq = AddChatHistoryReq.builder()
							.appId(appId)
							.messageType(MessageTypeEnum.AI.getValue())
							.message(errorMessage)
							.build();
					chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
				});
	}
}
