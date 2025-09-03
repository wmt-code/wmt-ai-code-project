package com.wmt.wmtaicode.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * 项目下载服务实习类
 */
@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {
	/**
	 * 忽略的文件和文件夹名称
	 */
	private static final Set<String> IGNORED_NAMES = Set.of(
			".git", ".svn", "node_modules", "target", "build", ".idea", ".vscode", ".DS_Store", "dist",
			".env"
	);
	/**
	 * 需要过滤的文件扩展名
	 */
	private static final Set<String> IGNORED_EXTENSIONS = Set.of(
			".log", ".tmp", ".cache"
	);

	/**
	 * 检查路径中是否包含忽略的文件或文件夹名称
	 *
	 * @param projectRoot 项目根路径
	 * @param fullPath    完整路径
	 * @return 如果包含忽略的名称则返回true，否则返回false
	 */
	public boolean containsIgnoredNamesAndExt(Path projectRoot, Path fullPath) {
		Path relativePath = projectRoot.relativize(fullPath);
		for (Path part : relativePath) {
			String partName = part.toString();
			// 检查是否在忽略名称列表中
			if (IGNORED_NAMES.contains(partName)) {
				return true;
			}
			// 检查是否是需要过滤的扩展名
			if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
		// 参数校验
		ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
		ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
		File file = new File(projectPath);
		ThrowUtils.throwIf(!file.exists() || !file.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径无效");
		// 下载项目
		log.info("开始下载项目，路径：{} -> {}.zip", projectPath, downloadFileName);
		// 设置响应头
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + ".zip\"");
		// 定义文件过滤器，排除不需要的文件和文件夹
		FileFilter fileFilter = pathname -> !containsIgnoredNamesAndExt(file.toPath(), pathname.toPath());
		try {
			// 使用ZipUtil将目录压缩并写入响应输出流
			ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, fileFilter, file);
			log.info("项目下载完成，路径：{} -> {}.zip", projectPath, downloadFileName);
		} catch (IOException e) {
			log.error("项目下载失败，路径：{} -> {}.zip, 错误信息: {}", projectPath, downloadFileName, e.getMessage());
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目下载失败");
		}
	}
}
