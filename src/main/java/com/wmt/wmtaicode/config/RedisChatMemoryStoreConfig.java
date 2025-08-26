package com.wmt.wmtaicode.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedisChatMemoryStore 配置类。
 * 从 application.properties 或 application.yml 中加载 Redis 相关配置。
 * 并创建 RedisChatMemoryStore Bean 以供应用程序使用。
 */
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
@Configuration
public class RedisChatMemoryStoreConfig {
	private String host;
	private int port;
	private long ttl;

	@Bean
	public RedisChatMemoryStore redisChatMemoryStore() {
		return RedisChatMemoryStore.builder()
				.host(host)
				.port(port)
				.ttl(ttl)
				.build();
	}
}
