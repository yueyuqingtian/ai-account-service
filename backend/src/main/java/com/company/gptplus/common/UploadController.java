package com.company.gptplus.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/admin/uploads")
public class UploadController {
    private static final Set<String> ALLOWED = Set.of("image/png", "image/jpeg", "image/webp", "image/gif");

    private final AuthSupport authSupport;
    private final Path uploadRoot;

    public UploadController(AuthSupport authSupport, @Value("${gpt-plus.upload.dir:uploads}") String uploadDir) {
        this.authSupport = authSupport;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping("/image")
    public ApiResponse<?> image(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        authSupport.requireAdmin(request);
        if (file.isEmpty()) {
            throw new BizException(70001, "上传文件不能为空");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new BizException(70002, "仅支持 png、jpg、webp、gif 图片");
        }
        try {
            String suffix = suffix(file.getOriginalFilename());
            String day = LocalDate.now().toString();
            Path dir = uploadRoot.resolve(day);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            String url = "/uploads/" + day + "/" + filename;
            return ApiResponse.ok(Map.of("url", url, "filename", filename));
        } catch (Exception ex) {
            throw new BizException(70003, "图片上传失败：" + ex.getMessage());
        }
    }

    private String suffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".png";
        }
        String value = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        if (value.length() > 8) {
            return ".png";
        }
        return value;
    }
}
