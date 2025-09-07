package com.wmt.wmtaicode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 通用流式聊天模型配置
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {
	private String apiKey;
	private String baseUrl;
	private String modelName;
	private Double temperature;
	private Integer maxTokens;
	private Boolean logRequests;
	private Boolean logResponses;

	@Bean
	@Scope("prototype") // 创建多个实例
	public StreamingChatModel streamingChatModelPrototype() {
		return OpenAiStreamingChatModel.builder()
				.apiKey(apiKey)
				.baseUrl(baseUrl)
				.modelName(modelName)
				.temperature(temperature)
				.maxTokens(maxTokens)
				.logRequests(logRequests)
				.logResponses(logResponses)
				.build();
	}
}
