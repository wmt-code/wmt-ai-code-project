package com.wmt.wmtaicode.core.parser;

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
		String htmlCode = extractHtmlCode(codeContent);
		if (htmlCode != null && !htmlCode.trim().isEmpty()) {
			htmlCodeResult.setHtmlCode(htmlCode.trim());
		} else {
			htmlCodeResult.setHtmlCode(codeContent);
		}
		return htmlCodeResult;
	}


	public static String extractHtmlCode(String codeContent) {
		if (codeContent == null || codeContent.isEmpty()) {
			return null;
		}
		var matcher = HTML_CODE_PATTERN.matcher(codeContent);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}
}
