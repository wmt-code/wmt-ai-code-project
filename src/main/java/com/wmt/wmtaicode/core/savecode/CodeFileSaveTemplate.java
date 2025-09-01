package com.wmt.wmtaicode.core.savecode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.constant.AppConstant;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;

import java.io.File;

/**
 * 代码文件保存模板类，提供统一的代码保存逻辑。
 * 子类需要实现具体的代码生成类型和文件保存逻辑。
 *
 * @param <T>
 */
public abstract class CodeFileSaveTemplate<T> {
	// 文件保存的根目录
	private static final String ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

	/**
	 * 保存生成的代码到文件系统中(通过appId关联应用)
	 *
	 * @param result 生成的代码结果
	 * @param appId  应用ID，用于关联生成的代码
	 * @return 保存的文件目录
	 */
	public final File saveCode(T result, Long appId) {
		// 验证输入
		validateInput(result);
		// 生成唯一路径
		String baseDir = buildUniqueDir(appId);
		// 保存文件
		saveFiles(baseDir, result);
		// 返回保存的目录
		return new File(baseDir);

	}

	/**
	 * 验证输入结果是否有效
	 *
	 * @param result 生成的代码结果
	 */
	protected void validateInput(T result) {
		ThrowUtils.throwIf(result == null, ErrorCode.PARAMS_ERROR, "生成的代码结果不能为空");
	}


	protected final String buildUniqueDir(Long appId) {
		String uniqueDir = StrUtil.format("{}_{}", getCodeGenTypeEnum().getValue(), appId);
		String dirPath = ROOT_DIR + File.separator + uniqueDir;
		// 创建目录
		FileUtil.mkdir(dirPath);
		return dirPath;
	}

	protected final void writeToFile(String baseDir, String fileName, String content) {
		String filePath = baseDir + File.separator + fileName;
		FileUtil.writeUtf8String(content, filePath);
	}

	protected abstract CodeGenTypeEnum getCodeGenTypeEnum();

	protected abstract void saveFiles(String baseDir, T result);
}
