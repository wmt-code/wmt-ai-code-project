package com.wmt.wmtaicode.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.wmt.wmtaicode.config.CosClientConfig;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.model.enums.FileTypeEnum;
import com.wmt.wmtaicode.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

	@Resource
	private COSClient cosClient;

	@Resource
	private CosClientConfig cosClientConfig;

	/**
	 * 支持MultipartFile 文件上传
	 *
	 * @param file         文件
	 * @param folder       文件夹名称
	 * @param fileTypeEnum 文件类型枚举
	 * @return 文件访问URL
	 */
	@Override
	public String uploadFile(MultipartFile file, String folder, FileTypeEnum fileTypeEnum) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
		}

		// 获取原始文件名
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
		}
		String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

		// 根据文件类型校验文件格式
		checkFileType(fileTypeEnum, suffix, originalFilename);

		// 根据文件类型设置不同的大小限制
		long maxSize = getMaxFileSizeByType(fileTypeEnum);
		if (file.getSize() > maxSize) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,
					String.format("文件大小不能超过%dMB", maxSize / (1024 * 1024)));
		}

		// 生成文件名：目录类型保持原名，其他类型生成UUID
		String fileName;
		if (fileTypeEnum == FileTypeEnum.DIRECTORY) {
			fileName = originalFilename; // 保持原始文件名
		} else {
			fileName = UUID.randomUUID().toString() + suffix; // 生成唯一文件名
		}

		String key = folder + "/" + fileName;

		try {
			// 设置对象元数据
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			metadata.setContentType(getContentTypeByFileType(fileTypeEnum, file.getContentType()));
			metadata.setContentDisposition("inline");

			// 创建上传请求
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					cosClientConfig.getBucket(),
					key,
					file.getInputStream(),
					metadata
			);

			// 上传文件
			PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
			log.info("文件上传成功，类型: {}, 文件名: {}, ETag: {}",
					fileTypeEnum.getText(), fileName, putObjectResult.getETag());

			// 返回文件访问URL
			return cosClientConfig.getHost() + "/" + key;

		} catch (IOException e) {
			log.error("文件上传失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
		} catch (Exception e) {
			log.error("COS上传失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
		}
	}

	/**
	 * 上传File对象
	 */
	@Override
	public String uploadFile(File file, String folder, FileTypeEnum fileTypeEnum) {
		if (file == null || !file.exists()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不存在");
		}

		// 获取文件名
		String originalFilename = file.getName();
		String suffix = "";
		int lastDotIndex = originalFilename.lastIndexOf(".");
		if (lastDotIndex > 0) {
			suffix = originalFilename.substring(lastDotIndex).toLowerCase();
		}

		// 根据文件类型校验文件格式（与MultipartFile版本相同的校验逻辑）
		checkFileType(fileTypeEnum, suffix, originalFilename);

		// 根据文件类型设置不同的大小限制
		long maxSize = getMaxFileSizeByType(fileTypeEnum);
		if (file.length() > maxSize) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,
					String.format("文件大小不能超过%dMB", maxSize / (1024 * 1024)));
		}

		// 生成文件名：目录类型保持原名，其他类型生成UUID
		String fileName;
		if (fileTypeEnum == FileTypeEnum.DIRECTORY) {
			fileName = originalFilename; // 保持原始文件名
		} else {
			fileName = UUID.randomUUID().toString() + suffix; // 生成唯一文件名
		}

		String key = folder + "/" + fileName;

		try {
			// 设置对象元数据
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.length());
			metadata.setContentType(getContentTypeByFileType(fileTypeEnum, null));
			metadata.setContentDisposition("inline");

			// 创建上传请求
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					cosClientConfig.getBucket(),
					key,
					file
			);
			putObjectRequest.withMetadata(metadata);

			// 上传文件
			PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
			log.info("文件上传成功，类型: {}, 文件名: {}, ETag: {}",
					fileTypeEnum.getText(), fileName, putObjectResult.getETag());

			// 返回文件访问URL
			return cosClientConfig.getHost() + "/" + key;

		} catch (Exception e) {
			log.error("COS上传失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
		}
	}

	/**
	 * 校验文件类型和格式
	 *
	 * @param fileTypeEnum     文件类型枚举
	 * @param suffix           文件后缀
	 * @param originalFilename 原始文件名
	 */
	private static void checkFileType(FileTypeEnum fileTypeEnum, String suffix, String originalFilename) {
		switch (fileTypeEnum) {
			case IMAGE:
				if (!suffix.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 jpg、jpeg、png、gif、bmp、webp 格式的图片");
				}
				break;
			case TEXT:
				if (!suffix.matches("\\.(txt|doc|docx|pdf|md|html|css|js|json|xml|csv|yml|yaml|properties|java|py" +
						"|sql)" +
						"$")) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 txt、doc、docx、pdf、md、html、css、js、json" +
							"、xml" +
							"、csv、yml、yaml、properties、java、py、sql 格式的文本文件");
				}
				break;
			case AUDIO:
				if (!suffix.matches("\\.(mp3|wav|flac|aac|ogg|m4a|wma)$")) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 mp3、wav、flac、aac、ogg、m4a、wma 格式的音频文件");
				}
				break;
			case VIDEO:
				if (!suffix.matches("\\.(mp4|avi|mov|wmv|flv|mkv|webm|m4v|3gp)$")) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 mp4、avi、mov、wmv、flv、mkv、webm、m4v、3gp " +
							"格式的视频文件");
				}
				break;
			case DIRECTORY:
				// 目录类型支持所有文件类型，不做格式限制
				log.info("目录类型文件：{}", originalFilename);
				break;
			default:
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
		}
	}

	/**
	 * 根据文件类型获取最大文件大小
	 */
	private long getMaxFileSizeByType(FileTypeEnum fileTypeEnum) {
		return switch (fileTypeEnum) {
			case IMAGE -> 10 * 1024 * 1024; // 10MB
			case TEXT -> 20 * 1024 * 1024; // 20MB
			case AUDIO -> 100 * 1024 * 1024; // 100MB
			case VIDEO -> 500 * 1024 * 1024; // 500MB
			case DIRECTORY -> 1024 * 1024 * 1024; // 1GB
			default -> 5 * 1024 * 1024; // 默认5MB
		};
	}

	/**
	 * 根据文件类型获取Content-Type
	 */
	private String getContentTypeByFileType(FileTypeEnum fileTypeEnum, String originalContentType) {
		if (originalContentType != null && !originalContentType.isEmpty()) {
			return originalContentType;
		}

		return switch (fileTypeEnum) {
			case IMAGE -> "image/*";
			case TEXT -> "text/plain";
			case AUDIO -> "audio/*";
			case VIDEO -> "video/*";
			default -> "application/octet-stream";
		};
	}

	@Override
	public boolean deleteFile(String fileUrl) {
		if (fileUrl == null || fileUrl.isEmpty()) {
			return false;
		}

		try {
			// 从URL中提取key
			String key = fileUrl.replace(cosClientConfig.getHost() + "/", "");

			// 删除文件
			cosClient.deleteObject(cosClientConfig.getBucket(), key);
			log.info("文件删除成功，key: {}", key);
			return true;

		} catch (Exception e) {
			log.error("文件删除失败", e);
			return false;
		}
	}
}
