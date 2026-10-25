package com.starlight.cms.controller.common;

import com.starlight.cms.common.Result;
import com.starlight.cms.exception.BusinessException;
import com.starlight.cms.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传（本地存储）：电影海报、宣传图
 * 返回的 url 为相对路径，前端拼接接口根地址即可访问
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${cms.upload.path}")
    private String uploadPath;

    @Value("${cms.upload.url-prefix}")
    private String urlPrefix;

    private static final List<String> ALLOW_EXT = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        // 仅员工/管理员可上传，避免匿名上传漏洞
        SecurityUtils.requireStaff();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf(".")).toLowerCase() : "";
        if (!ALLOW_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 图片");
        }
        // MIME 二次校验，拦截改后缀伪装的文件
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("文件类型不合法，请上传图片文件");
        }
        File dir = new File(uploadPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败：" + uploadPath);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
        Map<String, String> result = new HashMap<>();
        result.put("url", urlPrefix + fileName);
        result.put("fileName", fileName);
        return Result.ok("上传成功", result);
    }
}
