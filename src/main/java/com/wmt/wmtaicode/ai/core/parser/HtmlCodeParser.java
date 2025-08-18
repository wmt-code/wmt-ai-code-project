package com.wmt.wmtaicode.ai.core.parser;

import com.wmt.wmtaicode.ai.model.HTMLCodeResult;

import java.util.regex.Pattern;

/**
 * 解析html代码
 */
public class HtmlCodeParser implements CodeParser<HTMLCodeResult> {
	private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```",
			Pattern.CASE_INSENSITIVE);

	@Override
	public HTMLCodeResult parseCode(String codeContent) {
		HTMLCodeResult htmlCodeResult = new HTMLCodeResult();
		return null;
	}
}
