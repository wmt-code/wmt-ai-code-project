package com.wmt.wmtaicode.ai.core;

import com.wmt.wmtaicode.ai.AiCodeGeneratorService;
import com.wmt.wmtaicode.ai.core.parser.CodeParserExecutor;
import com.wmt.wmtaicode.ai.core.savecode.CodeFileSaveExecutor;
import com.wmt.wmtaicode.ai.model.HTMLCodeResult;
import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
	private AiCodeGeneratorService aiCodeGeneratorService;


	/**
	 * 统一接返回路径接口，生成代码-》保存文件
	 * 流式输出
	 *
	 * @param prompt          提示词
	 * @param codeGenTypeEnum 代码生成的类型
	 * @return 流式输出内容
	 */
	public Flux<String> generateAndSaveCodeStream(String prompt, CodeGenTypeEnum codeGenTypeEnum) {
		if (codeGenTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的代码类型不能为空");
		}
		return switch (codeGenTypeEnum) {
			case HTML -> {
				Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHTMLCodeStream(prompt);
				yield processCodeStream(htmlCodeStream,codeGenTypeEnum);
			}
			case MULTI_FILE -> {
				Flux<String> mutiFileCodeStream = aiCodeGeneratorService.generateMutiFileCodeStream(prompt);
				yield processCodeStream(mutiFileCodeStream,codeGenTypeEnum);
			}
			default ->
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum.getValue());
		};
	}


	/**
	 * 统一接返回路径接口，生成代码-》保存文件
	 *
	 * @param prompt          提示词
	 * @param codeGenTypeEnum 代码生成的类型
	 * @return 文件路径
	 */
	public File generateAndSaveCode(String prompt, CodeGenTypeEnum codeGenTypeEnum) {
		if (codeGenTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的代码类型不能为空");
		}
		return switch (codeGenTypeEnum) {
			case HTML -> {
				HTMLCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(prompt);
				yield CodeFileSaveExecutor.executeSaveCode(htmlCodeResult, codeGenTypeEnum);
			}
			case MULTI_FILE -> {
				MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMutiFileCode(prompt);
				yield CodeFileSaveExecutor.executeSaveCode(multiFileCodeResult, codeGenTypeEnum);
			}
			default ->
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum.getValue());
		};
	}

	private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum) {
		StringBuilder sb = new StringBuilder();
		return codeStream.doOnNext(sb::append
		).doOnComplete(() -> {
			try {
				String completeCode = sb.toString();
				// 解析代码
				Object result = CodeParserExecutor.executeCodeParser(completeCode, codeGenTypeEnum);
				// 保存代码
				File saveDir = CodeFileSaveExecutor.executeSaveCode(result, codeGenTypeEnum);
				log.info("代码生成完成，保存到: {}", saveDir.getAbsolutePath());
			} catch (Exception e) {
				log.error("代码生成失败：{}", e.getMessage());
			}
		});
	}


	// ----------------------------------------------------------------------------------

	/**
	 * 生成多文件代码并保存到文件
	 *
	 * @param prompt 提示词
	 * @return 文件路径
	 */
	private File generateMultiFileCode(String prompt) {
		MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMutiFileCode(prompt);
		if (multiFileCodeResult == null || multiFileCodeResult.getHtmlCode() == null) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成多文件代码失败");
		}
		return CodeFileSave.saveMutiFileCodeResult(multiFileCodeResult);
	}

	/**
	 * 生成多文件代码并保存到文件(流式)
	 *
	 * @param prompt 提示词
	 * @return 流式输出
	 */
	private Flux<String> generateMultiFileCodeStream(String prompt) {
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

	/**
	 * 生成HTML代码并保存到文件
	 *
	 * @param prompt 提示词
	 * @return 文件路径
	 */
	private File generateHTMLCode(String prompt) {
		HTMLCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(prompt);
		if (htmlCodeResult == null || htmlCodeResult.getHtmlCode() == null) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成HTML代码失败");
		}
		return CodeFileSave.saveHtmlCodeResult(htmlCodeResult);
	}

	/**
	 * 生成HTML代码并返回流式输出
	 *
	 * @param prompt 提示词
	 * @return 流式输出HTML代码
	 */
	private Flux<String> generateHTMLCodeStream(String prompt) {
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
	}
}
