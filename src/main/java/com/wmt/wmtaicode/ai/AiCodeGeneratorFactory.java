package com.wmt.wmtaicode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wmt.wmtaicode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * AI代码生成服务工厂类
 * 用于创建AI代码生成服务实例
 */
@Slf4j
@Configuration
public class AiCodeGeneratorFactory {
	@Resource
	private ChatModel chatModel;
	@Resource
	private StreamingChatModel streamingChatModel;
	@Resource
	private RedisChatMemoryStore redisChatMemoryStore;
	@Resource
	private ChatHistoryService chatHistoryService;
	/**
	 * 服务实例缓存，key为appId，value为AiCodeGeneratorService实例
	 * 最大缓存1000个实例，写入30分钟后过期，10分钟未访问后过期
	 * 移除时打印日志
	 */
	private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.removalListener((key, value, cause) -> {
				log.debug("AI服务实例被移除，key: {}, cause: {}", key, cause);
			}).build();


	private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
		// 根据appId分配对应的aiService进行会话历史隔离

		log.info("为appId: {} 创建新的AI服务实例", appId);
		MessageWindowChatMemory chatMemory = new MessageWindowChatMemory.Builder()
				.id(appId)
				.chatMemoryStore(redisChatMemoryStore)
				.maxMessages(20)
				.build();
		// 从数据库中加载历史消息
		chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
		return AiServices.builder(AiCodeGeneratorService.class)
				.chatModel(chatModel)
				// .chatModel(chatModel)
				.streamingChatModel(streamingChatModel)
				.chatMemory(chatMemory)
				.build();
	}

	/**
	 * 默认返回一个appId为0的服务实例
	 *
	 */
	public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
		return serviceCache.get(appId, this::createAiCodeGeneratorService);
	}
}
