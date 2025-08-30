package com.wmt.wmtaicode.ai;

import com.wmt.wmtaicode.ai.model.HTMLCodeResult;
import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI代码生成服务接口
 */
public interface AiCodeGeneratorService {

	String generateCode(String prompt);

	/**
	 * 生成HTML代码
	 *
	 * @param prompt 提示词
	 * @return 生成的HTML代码
	 */
	@SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
	HTMLCodeResult generateHTMLCode(@MemoryId int memoryId, @UserMessage String prompt);

	/**
	 * 生成多文件代码(html、css、js分离)
	 *
	 * @param prompt 提示词
	 * @return 代码
	 */
	@SystemMessage(fromResource = "prompt/codegen-muti-file-system-prompt.txt")
	MultiFileCodeResult generateMutiFileCode(String prompt);


	/**
	 * 生成HTML代码
	 *
	 * @param prompt 提示词
	 * @return 生成的HTML代码
	 */
	@SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
	Flux<String> generateHTMLCodeStream(String prompt);

	/**
	 * 生成多文件代码(html、css、js分离)
	 *
	 * @param prompt 提示词
	 * @return 代码
	 */
	@SystemMessage(fromResource = "prompt/codegen-muti-file-system-prompt.txt")
	Flux<String> generateMutiFileCodeStream(String prompt);

	/**
	 * 生成vue项目
	 *
	 * @param prompt 提示词
	 * @return 代码
	 */
	@SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
	TokenStream generateVueProjectCodeStream(@MemoryId Long appId, @UserMessage String prompt);
}
