package com.sandwich.ragkg.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 文件存储服务
 */
public interface MinioService {

    /**
     * 上传头像到 avatar 桶
     *
     * @param file 头像文件
     * @return 可访问的头像 URL
     */
    String uploadAvatar(MultipartFile file);
}
