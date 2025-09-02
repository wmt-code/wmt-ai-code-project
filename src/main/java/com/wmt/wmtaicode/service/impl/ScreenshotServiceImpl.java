package com.wmt.wmtaicode.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.model.enums.FileTypeEnum;
import com.wmt.wmtaicode.service.FileService;
import com.wmt.wmtaicode.service.ScreenshotService;
import com.wmt.wmtaicode.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 截图服务实现类
 */
@Service
public class ScreenshotServiceImpl implements ScreenshotService {
	@Resource
	private FileService fileService;

	/**
	 * 生成网页截图并上传，返回图片URL
	 *
	 * @param webUrl 网页URL
	 * @return 图片URL
	 */
	@Override
	public String generateAndUploadScreenshot(String webUrl) {
		// 1.生成截图
		String imagePath = WebScreenshotUtils.takeScreenshot(webUrl);
		ThrowUtils.throwIf(StrUtil.isBlank(imagePath), ErrorCode.PARAMS_ERROR, "截图生成失败");
		// 2.上传图片
		// 文件夹名称
		String folder = String.format("/screenshot/%s/", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
		String imageUrl = fileService.uploadFile(new File(imagePath), folder, FileTypeEnum.IMAGE);
		// 3.删除本地图片文件
		FileUtil.del(imagePath);
		// 4.返回图片URL
		return imageUrl;
	}
}
