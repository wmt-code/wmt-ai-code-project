package com.wmt.wmtaicode.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由
 * 根据用户输入的提示器，判断使用哪种代码生成类型
 */
@Configuration
public class AiCodeGenTypeRoutingFactory {
	@Resource
	private ChatModel chatModel;

	/**
	 * 创建AI代码生成类型路由服务实例
	 *
	 * @return AiCodeGenTypeRoutingService实例
	 */
	@Bean
	public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
		return AiServices.builder(AiCodeGenTypeRoutingService.class)
				.chatModel(chatModel)
				.build();
	}
}
