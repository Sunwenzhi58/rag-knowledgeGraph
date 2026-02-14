package com.sandwich.ragkg.service.impl;

import com.sandwich.ragkg.config.MinioProperties;
import com.sandwich.ragkg.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 文件存储服务实现
 */
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String uploadAvatar(MultipartFile file) {
        String bucketName = minioProperties.getBuckets().getAvatar();
        ensureBucketExists(bucketName);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String objectName = "avatar/" + UUID.randomUUID() + ext;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }

        return buildPublicUrl(bucketName, objectName);
    }

    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("检查或创建桶失败: " + e.getMessage());
        }
    }

    private String buildPublicUrl(String bucketName, String objectName) {
        String publicUrl = minioProperties.getPublicUrl();
        if (publicUrl == null || publicUrl.isEmpty()) {
            publicUrl = minioProperties.getEndpoint();
        }
        String base = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
        return base + "/" + bucketName + "/" + objectName;
    }
}
