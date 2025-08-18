package com.wmt.wmtaicode.ai.core.parser;

/**
 * 代码解析器，提取ai生成的代码中的有用信息
 *
 * @param <T>
 */
public interface CodeParser<T> {

	/**
	 * 解析代码
	 *
	 * @param codeContent 原始代码
	 * @return 解析后的结果
	 */
	T parseCode(String codeContent);
}
