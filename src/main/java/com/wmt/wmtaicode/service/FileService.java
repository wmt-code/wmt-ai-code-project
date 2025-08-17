package com.wmt.wmtaicode.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileService {

    /**
     * 上传文件到腾讯云COS
     *
     * @param file 文件
     * @param folder 文件夹名称
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}
