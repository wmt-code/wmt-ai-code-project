package com.wmt.wmtaicode.core.handler;

import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流式处理执行器
 * 根据不同的消息类型，执行相应的处理逻辑
 * 传统的Flux处理-> (HTML、MULTI_FILE) SimpleStreamHandler
 * 复杂的TokenStream处理-> (VUE_PROJECT)
 */
@Slf4j
@Component
public class StreamHandlerExecutor {
	@Resource
	private JsonMessageStreamHandler jsonMessageStreamHandler;

	/**
	 * 执行流式处理
	 * @param originFlux 原始流
	 * @param chatHistoryService 聊天记录服务
	 * @param appId 应用ID
	 * @param loginUser 登录用户
	 * @param codeGenTypeEnum 代码生成类型枚举
	 * @return 处理后的流
	 */
	public Flux<String> doExecute(Flux<String> originFlux,
								  ChatHistoryService chatHistoryService,
								  long appId,
								  UserVO loginUser,
								  CodeGenTypeEnum codeGenTypeEnum) {
		return switch (codeGenTypeEnum) {
			case HTML, MULTI_FILE ->
					new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
			case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
		};
	}
}
