package com.wmt.wmtaicode;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = RedisEmbeddingStoreAutoConfiguration.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan("com.wmt.wmtaicode.mapper")
public class WmtAiCodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WmtAiCodeApplication.class, args);
	}

}
