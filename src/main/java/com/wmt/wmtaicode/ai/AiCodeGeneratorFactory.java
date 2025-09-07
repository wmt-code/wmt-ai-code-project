package com.wmt.wmtaicode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.ai.tools.*;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.service.ChatHistoryService;
import com.wmt.wmtaicode.utils.SpringContextUtil;
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
	@Resource(name = "openAiChatModel")
	private ChatModel chatModel;
	@Resource
	private StreamingChatModel openAiStreamingChatModel;
	@Resource
	private StreamingChatModel reasoningStreamChatModel;
	@Resource
	private RedisChatMemoryStore redisChatMemoryStore;
	@Resource
	private ChatHistoryService chatHistoryService;
	@Resource
	private ToolManager toolManager;
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


	/**
	 * 创建新的 AI 服务实例
	 */
	private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
		// 根据 appId 构建独立的对话记忆
		MessageWindowChatMemory chatMemory = MessageWindowChatMemory
				.builder()
				.id(appId)
				.chatMemoryStore(redisChatMemoryStore)
				.maxMessages(50)
				.build();
		// 从数据库加载历史对话到记忆中
		chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
		// 根据代码生成类型选择不同的模型配置
		return switch (codeGenType) {
			// Vue 项目生成使用推理模型
			case VUE_PROJECT -> {
				// 使用多例模式，避免不同用户之间的对话内容相互影响
				StreamingChatModel reasoningStreamingChatModelPrototype = SpringContextUtil.getBean(
						"reasoningStreamingChatModelPrototype", StreamingChatModel.class);
				yield AiServices.builder(AiCodeGeneratorService.class)
						.streamingChatModel(reasoningStreamingChatModelPrototype)
						.chatMemoryProvider(memoryId -> chatMemory)
						.tools(toolManager.getAllTools())
						.hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
								toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
						))
						.build();
			}
			// HTML 和多文件生成使用默认模型
			case HTML, MULTI_FILE -> {
				StreamingChatModel streamingChatModelPrototype = SpringContextUtil.getBean(
						"streamingChatModelPrototype", StreamingChatModel.class);
				yield AiServices.builder(AiCodeGeneratorService.class)
						.chatModel(chatModel)
						.streamingChatModel(streamingChatModelPrototype)
						.chatMemory(chatMemory)
						.build();
			}
			default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
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
