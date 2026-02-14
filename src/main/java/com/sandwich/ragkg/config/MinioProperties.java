package com.sandwich.ragkg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 桶配置
     */
    private Buckets buckets = new Buckets();

    /**
     * 公网访问地址（用于生成文件访问 URL）
     */
    private String publicUrl;

    @Data
    public static class Buckets {
        /**
         * 文件上传桶
         */
        private String files = "uploads";

        /**
         * 头像存储桶
         */
        private String avatar = "avatar";
    }
}
