package com.wmt.wmtaicode.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务接口
 */
public interface ProjectDownloadService {
	void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
