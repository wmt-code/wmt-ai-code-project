package com.wmt.wmtaicode.ai.core.parser;

import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;

/**
 * 代码解析执行器，根据不同的代码类型执行相应的解析器。
 * 目前支持HTML、CSS、JS等代码的解析。
 */
public class CodeParserExecutor {
	private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
	private static final MutiFileCodeParser mutiFileCodeParser = new MutiFileCodeParser();

	/**
	 * 执行代码解析器，根据代码生成类型解析代码内容。
	 *
	 * @param codeContent     原始代码内容
	 * @param codeGenTypeEnum 代码生成类型枚举
	 * @return 解析后的结果
	 */
	public static Object executeCodeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
		if (codeContent == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码类型不能为空");
		}
		return switch (codeGenTypeEnum) {
			case HTML -> htmlCodeParser.parseCode(codeContent);
			case MULTI_FILE -> mutiFileCodeParser.parseCode(codeContent);
		};
	}

}
