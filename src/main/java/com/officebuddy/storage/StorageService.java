package com.officebuddy.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StorageResult uploadFile(MultipartFile file);
    StorageResult uploadBytes(String fileName, byte[] data, String contentType);
    void deleteFile(String key);
    String getPresignedUrl(String key);
    byte[] downloadBytes(String key);
}
