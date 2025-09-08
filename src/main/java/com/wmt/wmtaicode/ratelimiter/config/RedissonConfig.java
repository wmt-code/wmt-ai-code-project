package com.wmt.wmtaicode.ratelimiter.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private Integer redisPort;

	@Value("${spring.data.redis.password}")
	private String redisPassword;

	@Value("${spring.data.redis.database}")
	private Integer redisDatabase;

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		String address = "redis://" + redisHost + ":" + redisPort;
		config.useSingleServer()
				.setAddress(address)
				// 设置连接池最小空闲连接数
				.setConnectionMinimumIdleSize(1)
				// 设置连接池最大连接数
				.setConnectionPoolSize(10)
				.setPassword(redisPassword)
				.setDatabase(redisDatabase)
				// 命令等待超时时间d
				.setTimeout(3000)
				// 设置空闲连接超时时间
				.setIdleConnectionTimeout(30000)
				.setRetryAttempts(3)
				.setRetryInterval(1500)
				.setConnectTimeout(5000);
		return Redisson.create(config);
	}
}
