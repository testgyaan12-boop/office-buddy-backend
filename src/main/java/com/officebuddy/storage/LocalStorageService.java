package com.officebuddy.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public StorageResult uploadFile(MultipartFile file) {
        var originalName = file.getOriginalFilename();
        var extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        var key = UUID.randomUUID().toString() + extension;
        var targetPath = uploadPath.resolve(key);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        var url = "/api/v1/storage/" + key;
        return new StorageResult(key, url);
    }

    @Override
    public StorageResult uploadBytes(String fileName, byte[] data, String contentType) {
        var key = UUID.randomUUID().toString() + "-" + fileName;
        var targetPath = uploadPath.resolve(key);
        try {
            Files.write(targetPath, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store bytes", e);
        }
        var url = "/api/v1/storage/" + key;
        return new StorageResult(key, url);
    }

    @Override
    public void deleteFile(String key) {
        try {
            var filePath = uploadPath.resolve(key);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public String getPresignedUrl(String key) {
        return "/api/v1/storage/" + key;
    }

    @Override
    public byte[] downloadBytes(String key) {
        try {
            Path filePath = uploadPath.resolve(key);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + key, e);
        }
    }
}
