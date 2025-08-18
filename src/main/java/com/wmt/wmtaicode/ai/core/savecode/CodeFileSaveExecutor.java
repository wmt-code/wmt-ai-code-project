package com.wmt.wmtaicode.ai.core.savecode;

import com.wmt.wmtaicode.ai.model.HTMLCodeResult;
import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;

import java.io.File;

/**
 * 代码文件保存执行器，根据不同的代码类型执行相应的保存逻辑。
 * 目前支持HTML和多文件代码的保存。
 */
public class CodeFileSaveExecutor {
	private static final HtmlCodeSaveTemplate htmlCodeSaveTemplate = new HtmlCodeSaveTemplate();
	private static final MutiFileCodeFileSaveTemplate mutiFileCodeFileSaveTemplate =
			new MutiFileCodeFileSaveTemplate();

	/**
	 * 执行代码保存，根据代码类型进行相应的保存操作。
	 *
	 * @param codeResult      生成的代码结果对象，可能是HTMLCodeResult或MultiFileCodeResult。
	 * @param codeGenTypeEnum 代码生成类型枚举，指示代码的类型。
	 * @return 保存后的文件对象。
	 */
	public static File executeSaveCode(Object codeResult, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
		ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "代码类型不能为空");
		return switch (codeGenTypeEnum) {
			case HTML -> htmlCodeSaveTemplate.saveCode((HTMLCodeResult) codeResult,appId);
			case MULTI_FILE -> mutiFileCodeFileSaveTemplate.saveCode((MultiFileCodeResult) codeResult,appId);
		};
	}
}
