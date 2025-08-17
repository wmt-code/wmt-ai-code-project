package com.wmt.wmtaicode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan("com.wmt.wmtaicode.mapper")
public class WmtAiCodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WmtAiCodeApplication.class, args);
	}

}
