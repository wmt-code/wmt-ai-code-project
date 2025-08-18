package com.wmt.wmtaicode.ai;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成服务工厂类
 * 用于创建AI代码生成服务实例
 */
@Configuration
public class AiCodeGeneratorFactory {
	// @Resource
	// private ChatModel chatModel;
	@Resource
	private StreamingChatModel streamingChatModel;

	@Bean
	public AiCodeGeneratorService aiCodeGeneratorService() {
		return AiServices.builder(AiCodeGeneratorService.class)
				// .chatModel(chatModel)
				.streamingChatModel(streamingChatModel)
				.build();
	}
}
