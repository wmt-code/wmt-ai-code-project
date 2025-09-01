package com.wmt.wmtaicode.core.parser;

import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;

import java.util.regex.Pattern;

/**
 * 解析ai生成的html、css、js代码
 */
public class MutiFileCodeParser implements CodeParser<MultiFileCodeResult> {
	private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```",
			Pattern.CASE_INSENSITIVE);

	@Override
	public MultiFileCodeResult parseCode(String codeContent) {
		MultiFileCodeResult result = new MultiFileCodeResult();
		// 提取各类代码
		String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
		String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
		String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

		// 设置HTML代码
		if (htmlCode != null && !htmlCode.trim().isEmpty()) {
			result.setHtmlCode(htmlCode.trim());
		}
		// 设置CSS代码
		if (cssCode != null && !cssCode.trim().isEmpty()) {
			result.setCssCode(cssCode.trim());
		}
		// 设置JS代码
		if (jsCode != null && !jsCode.trim().isEmpty()) {
			result.setJsCode(jsCode.trim());
		}

		return result;
	}

	public static String extractCodeByPattern(String codeContent, Pattern pattern) {
		if (codeContent == null || codeContent.isEmpty()) {
			return null;
		}
		var matcher = pattern.matcher(codeContent);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}
}
