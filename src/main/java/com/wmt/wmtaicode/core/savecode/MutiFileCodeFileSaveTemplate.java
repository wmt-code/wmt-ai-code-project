package com.wmt.wmtaicode.core.savecode;

import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;

/**
 * 多文件代码文件保存模板
 * 处理多文件代码的保存逻辑，包括HTML、CSS、JS等文件。
 * 继承自CodeFileSaveTemplate，重写相关方法以适应多文件
 */
public class MutiFileCodeFileSaveTemplate extends CodeFileSaveTemplate<MultiFileCodeResult> {
	@Override
	protected CodeGenTypeEnum getCodeGenTypeEnum() {
		return CodeGenTypeEnum.MULTI_FILE;
	}

	@Override
	protected void saveFiles(String baseDir, MultiFileCodeResult result) {
		writeToFile(baseDir, "index.html", result.getHtmlCode());
		writeToFile(baseDir, "style.css", result.getCssCode());
		writeToFile(baseDir, "script.js", result.getJsCode());
	}

	@Override
	protected void validateInput(MultiFileCodeResult result) {
		super.validateInput(result);
		ThrowUtils.throwIf(result == null, ErrorCode.PARAMS_ERROR, "多文件代码结果不能为空");
	}
}
