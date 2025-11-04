package com.wmt.wmtaicode.ai.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wmt.wmtaicode.ai.model.HTMLCodeResult;
import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 保存ai生成的代码
 */
public class CodeFileSave {
	// 文件保存的根目录
	private static final String ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

	/**
	 * 保存html文件
	 *
	 * @param htmlCodeResult HTML代码结果对象
	 * @return 文件对象
	 */
	public static File saveHtmlCodeResult(HTMLCodeResult htmlCodeResult) {
		String baseDir = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
		writeToFile(baseDir, "index.html", htmlCodeResult.getHtmlCode());
		return new File(baseDir);
	}

	/**
	 * 保存多文件代码结果
	 *
	 * @param multiFileCodeResult 多文件代码结果对象
	 * @return 文件对象
	 */
	public static File saveMutiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
		String baseDir = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
		writeToFile(baseDir, "index.html", multiFileCodeResult.getHtmlCode());
		writeToFile(baseDir, "style.css", multiFileCodeResult.getCssCode());
		writeToFile(baseDir, "script.js", multiFileCodeResult.getJsCode());
		return new File(baseDir);
	}

	/**
	 * 写入单个文件
	 *
	 * @param baseDir  基础目录
	 * @param fileName 文件名
	 * @param content  文件内容
	 */
	private static void writeToFile(String baseDir, String fileName, String content) {
		String filePath = baseDir + File.separator + fileName;
		FileUtil.writeUtf8String(content, filePath);
	}


	/**
	 * 构建唯一的目录路径 /tmp/code_output/bizType_雪花id
	 */
	public static String buildUniqueDir(String bizType) {
		String uniqueDir = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextId());
		String dirPath = ROOT_DIR + File.separator + uniqueDir;
		// 创建目录
		FileUtil.mkdir(dirPath);
		return dirPath;
	}
}
