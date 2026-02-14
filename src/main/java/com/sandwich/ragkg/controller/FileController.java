package com.sandwich.ragkg.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.sandwich.ragkg.common.Result;
import com.sandwich.ragkg.service.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    /**
     * 头像上传（存储到 MinIO avatar 桶）
     */
    @PostMapping("/avatar/upload")
    public Result uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("500", "请选择要上传的头像文件");
        }
        String url = minioService.uploadAvatar(file);
        return Result.success(url);
    }

}
