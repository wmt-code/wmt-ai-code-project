package com.wmt.wmtaicode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.ai.tools.FileWriteTool;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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
	private StreamingChatModel openAiStreamingChatModel;
	@Resource
	private StreamingChatModel reasoningStreamChatModel;
	@Resource
	private RedisChatMemoryStore redisChatMemoryStore;
	@Resource
	private ChatHistoryService chatHistoryService;
	/**
	 * 服务实例缓存，key为appId，value为AiCodeGeneratorService实例
	 * 最大缓存1000个实例，写入30分钟后过期，10分钟未访问后过期
	 * 移除时打印日志
	 */
	private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.removalListener((key, value, cause) -> {
				log.debug("AI服务实例被移除，key: {}, cause: {}", key, cause);
			}).build();


	private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
		// 根据appId分配对应的aiService进行会话历史隔离

		log.info("为appId: {} 创建新的AI服务实例", appId);
		MessageWindowChatMemory chatMemory = new MessageWindowChatMemory.Builder()
				.id(appId)
				.chatMemoryStore(redisChatMemoryStore)
				.maxMessages(20)
				.build();
		// 从数据库中加载历史消息
		chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
		return switch (codeGenTypeEnum) {
			case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
					.chatModel(chatModel)
					// .chatModel(chatModel)
					.streamingChatModel(openAiStreamingChatModel)
					.chatMemory(chatMemory)
					.build();
			case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
					.streamingChatModel(reasoningStreamChatModel)
					.chatMemoryProvider(memoryId -> chatMemory)
					.tools(new FileWriteTool())
					.hallucinatedToolNameStrategy(toolExecutionRequest ->
							ToolExecutionResultMessage.from(toolExecutionRequest,
									"无法执行工具: " + toolExecutionRequest.name() + "，请检查工具名称是否正确"))
					.build();
			default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum);
		};
	}

	/**
	 * 根据 appId 获取服务（带缓存）这个方法是为了兼容历史逻辑
	 */
	public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
		return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
	}

	/**
	 * 根据 appId 和代码生成类型获取服务（带缓存）
	 */
	public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
		String cacheKey = buildCacheKey(appId, codeGenType);
		return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
	}

	/**
	 * 构建缓存键
	 */
	private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
		return appId + "_" + codeGenType.getValue();
	}

}
