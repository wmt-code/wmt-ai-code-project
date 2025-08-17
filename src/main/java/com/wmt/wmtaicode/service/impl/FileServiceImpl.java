package com.wmt.wmtaicode.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.wmt.wmtaicode.config.CosClientConfig;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        // 校验文件类型
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!suffix.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 jpg、jpeg、png、gif、bmp、webp 格式的图片");
        }

        // 校验文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + suffix;
        String key = folder + "/" + fileName;

        try {
            // 设置对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosClientConfig.getBucket(),
                    key,
                    file.getInputStream(),
                    metadata
            );

            // 上传文件
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            log.info("文件上传成功，ETag: {}", putObjectResult.getETag());

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
