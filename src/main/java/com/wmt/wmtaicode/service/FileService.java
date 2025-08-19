package com.wmt.wmtaicode.service;

import com.wmt.wmtaicode.model.enums.FileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件上传服务接口
 */
public interface FileService {

	/**
	 * 上传文件到腾讯云COS
	 *
	 * @param file   MultipartFile文件
	 * @param folder 文件夹名称
	 * @return 文件访问URL
	 */
	String uploadFile(MultipartFile file, String folder, FileTypeEnum fileTypeEnum);

	/**
	 * 上传文件到腾讯云COS
	 *
	 * @param file         File文件
	 * @param folder       文件目录
	 * @param fileTypeEnum 文件类型枚举
	 * @return 文件访问URL
	 */
	String uploadFile(File file, String folder, FileTypeEnum fileTypeEnum);

	/**
	 * 删除文件
	 *
	 * @param fileUrl 文件URL
	 * @return 是否删除成功
	 */
	boolean deleteFile(String fileUrl);
}
