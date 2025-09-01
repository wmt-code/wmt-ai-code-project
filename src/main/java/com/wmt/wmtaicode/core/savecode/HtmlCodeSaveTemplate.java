package com.wmt.wmtaicode.core.savecode;

import com.wmt.wmtaicode.ai.model.HTMLCodeResult;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;

/**
 * HTML代码保存模板类，用于处理HTML代码的保存逻辑。
 * 继承自CodeFileSaveTemplate，重写保存文件方法。
 */
public class HtmlCodeSaveTemplate extends CodeFileSaveTemplate<HTMLCodeResult> {
	@Override
	protected CodeGenTypeEnum getCodeGenTypeEnum() {
		return CodeGenTypeEnum.HTML;
	}

	@Override
	protected void saveFiles(String baseDir, HTMLCodeResult result) {
		writeToFile(baseDir, "index.html", result.getHtmlCode());
	}

	@Override
	protected void validateInput(HTMLCodeResult result) {
		super.validateInput(result);
		if (result.getHtmlCode() == null || result.getHtmlCode().isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML代码不能为空");
		}
	}
}
