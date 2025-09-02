package com.wmt.wmtaicode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamChatModelConfig {
	private String apiKey;
	private String baseUrl;

	/**
	 * vue工程化代码生成模型
	 *
	 * @return StreamingChatModel
	 */
	@Bean
	public StreamingChatModel reasoningStreamChatModel() {
		// 测试环境使用deepseek-chat模型
		// final String modelName = "deepseek-chat";
		// final int maxTokens = 8192;
		// 生产环境使用deepseek-reasoner模型
		final String modelName = "deepseek-reasoner";
		final int maxTokens = 32768;
		return OpenAiStreamingChatModel.builder()
				.apiKey(apiKey)
				.baseUrl(baseUrl)
				.modelName(modelName)
				.maxTokens(maxTokens)
				.logRequests(true)
				.logResponses(true)
				.build();

	}
}
