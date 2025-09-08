package com.wmt.wmtaicode.core;

import cn.hutool.json.JSONUtil;
import com.wmt.wmtaicode.ai.AiCodeGeneratorFactory;
import com.wmt.wmtaicode.ai.AiCodeGeneratorService;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.ai.model.message.AiResponseMessage;
import com.wmt.wmtaicode.ai.model.message.ToolExecutedRequestMessage;
import com.wmt.wmtaicode.ai.model.message.ToolExecutedResultMessage;
import com.wmt.wmtaicode.constant.AppConstant;
import com.wmt.wmtaicode.core.builder.VueProjectBuilder;
import com.wmt.wmtaicode.core.parser.CodeParserExecutor;
import com.wmt.wmtaicode.core.savecode.CodeFileSaveExecutor;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * Ai代码生成门面类，统一代码生成与文件保存
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
	@Resource
	private VueProjectBuilder vueProjectBuilder;
	@Lazy
	@Resource
	private AiCodeGeneratorFactory aiCodeGeneratorFactory;


	/**
	 * 统一接返回路径接口，生成代码-》保存文件
	 * 流式输出
	 *
	 * @param prompt          提示词
	 * @param codeGenTypeEnum 代码生成的类型
	 * @return 流式输出内容
	 */
	public Flux<String> generateAndSaveCodeStream(String prompt, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
		if (codeGenTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的代码类型不能为空");
		}
		AiCodeGeneratorService aiCodeGeneratorService =
				aiCodeGeneratorFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
		return switch (codeGenTypeEnum) {
			case HTML -> {
				Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHTMLCodeStream(prompt);
				yield processCodeStream(htmlCodeStream, codeGenTypeEnum, appId);
			}
			case MULTI_FILE -> {
				Flux<String> mutiFileCodeStream = aiCodeGeneratorService.generateMutiFileCodeStream(prompt);
				yield processCodeStream(mutiFileCodeStream, codeGenTypeEnum, appId);
			}
			case VUE_PROJECT -> {
				TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, prompt);
				yield processTokenStream(tokenStream, appId);
			}
			default ->
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum.getValue());
		};
	}


	private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
		StringBuilder sb = new StringBuilder();
		return codeStream.doOnNext(sb::append
				)
				.doOnComplete(() -> {
					try {
						String completeCode = sb.toString();
						// 解析代码
						Object result = CodeParserExecutor.executeCodeParser(completeCode, codeGenTypeEnum);
						// 保存代码
						File saveDir = CodeFileSaveExecutor.executeSaveCode(result, codeGenTypeEnum, appId);
						log.info("代码生成完成，保存到: {}", saveDir.getAbsolutePath());
					} catch (Exception e) {
						log.error("代码生成失败：{}", e.getMessage());
					}
				})
				.onErrorResume(throwable -> {
					// 发生错误时返回错误信息而不是中断流
					return Flux.just("代码生成过程中发生错误，请稍后重试");
				});
	}

	/**
	 * 将TokenStream转换为Flux流
	 *
	 * @param tokenStream TokenStream对象
	 * @return Flux流
	 */
	private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
		return Flux.create(sink -> {
			tokenStream.onPartialResponse((String partialResponse) -> {
				AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
				sink.next(JSONUtil.toJsonStr(aiResponseMessage));
			}).onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
				ToolExecutedRequestMessage toolExecutedRequestMessage =
						new ToolExecutedRequestMessage(toolExecutionRequest);
				sink.next(JSONUtil.toJsonStr(toolExecutedRequestMessage));
			}).onToolExecuted((toolExecution -> {
				ToolExecutedResultMessage toolExecutedResultMessage = new ToolExecutedResultMessage(toolExecution);
				sink.next(JSONUtil.toJsonStr(toolExecutedResultMessage));
			})).onCompleteResponse((ChatResponse chatResponse) -> {
				// 执行完后，进行vue项目的构建和打包
				String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
				vueProjectBuilder.buildProject(projectPath);
				sink.complete();
			}).onError((Throwable error) -> {
				error.printStackTrace();
				sink.error(error);
			}).start();
		});
	}

	// ----------------------------------------------------------------------------------
	// /**
	//  * 统一接返回路径接口，生成代码-》保存文件
	//  *
	//  * @param prompt          提示词
	//  * @param codeGenTypeEnum 代码生成的类型
	//  * @return 文件路径
	//  */
	/*
	public File generateAndSaveCode(int memoryId, String prompt, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
		if (codeGenTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的代码类型不能为空");
		}
		AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId);
		return switch (codeGenTypeEnum) {
			case HTML -> {
				HTMLCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(memoryId, prompt);
				yield CodeFileSaveExecutor.executeSaveCode(htmlCodeResult, codeGenTypeEnum, appId);
			}
			case MULTI_FILE -> {
				MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMutiFileCode(prompt);
				yield CodeFileSaveExecutor.executeSaveCode(multiFileCodeResult, codeGenTypeEnum, appId);
			}
			default ->
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum.getValue());
		};
	} */


	/**
	 * 生成多文件代码并保存到文件
	 *
	 * @param prompt 提示词
	 * @return 文件路径
	 *//*
	private File generateMultiFileCode(String prompt) {
		AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId);
		MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMutiFileCode(prompt);
		if (multiFileCodeResult == null || multiFileCodeResult.getHtmlCode() == null) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成多文件代码失败");
		}
		return CodeFileSave.saveMutiFileCodeResult(multiFileCodeResult);
	}

	 *//**
	 * 生成多文件代码并保存到文件(流式)
	 *
	 * @param prompt 提示词
	 * @return 流式输出
	 *//*
	private Flux<String> generateMultiFileCodeStream(String prompt) {
		AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId);
		Flux<String> result = aiCodeGeneratorService.generateMutiFileCodeStream(prompt);
		StringBuilder codeBuilder = new StringBuilder();
		return result.doOnNext(codeBuilder::append)
				.doOnComplete(() -> {
					// 流式完成后保存代码
					try {
						String completeMutiFileCode = codeBuilder.toString();
						MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMutiFileCode);
						File file = CodeFileSave.saveMutiFileCodeResult(multiFileCodeResult);
						log.info("多文件代码生成完成，保存到: {}", file.getAbsolutePath());
					} catch (Exception e) {
						log.error("多文件代码生成失败：{}", e.getMessage());
					}
				});
	}

	 *//**
	 * 生成HTML代码并保存到文件
	 *
	 * @param prompt 提示词
	 * @return 文件路径
	 *//*
	private File generateHTMLCode(int memoryId, String prompt) {
		AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId);
		HTMLCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(memoryId, prompt);
		if (htmlCodeResult == null || htmlCodeResult.getHtmlCode() == null) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成HTML代码失败");
		}
		return CodeFileSave.saveHtmlCodeResult(htmlCodeResult);
	}

	 *//**
	 * 生成HTML代码并返回流式输出
	 *
	 * @param prompt 提示词
	 * @return 流式输出HTML代码
	 *//*
	private Flux<String> generateHTMLCodeStream(String prompt) {
		AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId);
		Flux<String> result = aiCodeGeneratorService.generateHTMLCodeStream(prompt);
		StringBuilder codeBuilder = new StringBuilder();
		return result.doOnNext(codeBuilder::append)
				.doOnComplete(() -> {
					// 流式完成后保存代码
					try {
						String completeHtmlCode = codeBuilder.toString();
						HTMLCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
						File file = CodeFileSave.saveHtmlCodeResult(htmlCodeResult);
						log.info("HTML代码生成完成，保存到: {}", file.getAbsolutePath());
					} catch (Exception e) {
						log.error("HTML代码生成失败：{}", e.getMessage());
					}
				});
	} */
}
