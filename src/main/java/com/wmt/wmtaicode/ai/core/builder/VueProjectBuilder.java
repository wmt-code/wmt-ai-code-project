package com.wmt.wmtaicode.ai.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Vue项目构建器，用于安装依赖和打包
 */
@Slf4j
@Component
public class VueProjectBuilder {

	/**
	 * 异步构建 Vue 项目
	 *
	 * @param projectPath Vue 项目根目录路径
	 */
	public void buildProjectAsync(String projectPath) {
		Thread.ofVirtual().name("VueProjectBuilder-Thread-" + System.currentTimeMillis()).start(() -> {
			boolean success = buildProject(projectPath);
			if (success) {
				log.info("异步构建 Vue 项目成功: {}", projectPath);
			} else {
				log.error("异步构建 Vue 项目失败: {}", projectPath);
			}
		});
	}


	/**
	 * 构建 Vue 项目
	 *
	 * @param projectPath Vue 项目根目录路径
	 * @return 是否构建成功
	 */
	public boolean buildProject(String projectPath) {

		File projectDir = new File(projectPath);
		if (!projectDir.exists() || !projectDir.isDirectory()) {
			log.error("项目目录不存在或不是目录: {}", projectPath);
			return false;
		}
		// 检查 package.json 是否存在
		File packageJson = new File(projectDir, "package.json");
		if (!packageJson.exists()) {
			log.error("package.json 文件不存在，无法构建项目: {}", packageJson.getAbsolutePath());
			return false;
		}
		// 执行 npm install
		boolean installSuccess = executeNpmInstall(projectDir);
		if (!installSuccess) {
			log.error("npm install 失败");
			return false;
		}
		// 执行 npm run build
		boolean buildSuccess = executeNpmRunBuild(projectDir);
		if (!buildSuccess) {
			log.error("npm run build 失败");
			return false;
		}
		log.info("项目构建成功: {}", projectPath);
		return true;
	}


	/**
	 * 执行命令
	 *
	 * @param workingDir     工作目录
	 * @param command        命令字符串
	 * @param timeoutSeconds 超时时间（秒）
	 * @return 是否执行成功
	 */
	private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
		try {
			log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
			Process process = RuntimeUtil.exec(
					null,
					workingDir,
					command.split("\\s+") // 命令分割为数组
			);
			// 等待进程完成，设置超时
			boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
			if (!finished) {
				log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
				process.destroyForcibly();
				return false;
			}
			int exitCode = process.exitValue();
			if (exitCode == 0) {
				log.info("命令执行成功: {}", command);
				return true;
			} else {
				log.error("命令执行失败，退出码: {}", exitCode);
				return false;
			}
		} catch (Exception e) {
			log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
			return false;
		}
	}

	private boolean executeNpmInstall(File projectDir) {
		log.info("开始执行 npm install，项目目录: {}", projectDir.getAbsolutePath());
		String command = String.format("%s install", buildCommand());
		return executeCommand(projectDir, command, 300);
	}

	private boolean executeNpmRunBuild(File projectDir) {
		log.info("开始执行 npm run build，项目目录: {}", projectDir.getAbsolutePath());
		String command = String.format("%s run build", buildCommand());
		return executeCommand(projectDir, command, 300);
	}


	private boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	private String buildCommand() {
		if (isWindows()) {
			return "npm" + ".cmd";
		}
		return "npm";
	}
}
