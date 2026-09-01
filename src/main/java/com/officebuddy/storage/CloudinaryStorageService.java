package com.officebuddy.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(
            @Value("${app.storage.cloudinary.cloud-name}") String cloudName,
            @Value("${app.storage.cloudinary.api-key}") String apiKey,
            @Value("${app.storage.cloudinary.api-secret}") String apiSecret
    ) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public StorageResult uploadFile(MultipartFile file) {
        try {
            var originalName = file.getOriginalFilename();
            var publicId = "documents/" + UUID.randomUUID().toString();

            Map<String, Object> params = ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "auto"
            );

            if (originalName != null) {
                var extension = "";
                if (originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                params.put("filename_override", originalName);
            }

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            var url = (String) uploadResult.get("secure_url");
            var key = publicId;

            return new StorageResult(key, url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public StorageResult uploadBytes(String fileName, byte[] data, String contentType) {
        try {
            var publicId = "packs/" + UUID.randomUUID().toString();
            var params = ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "auto",
                    "filename_override", fileName
            );
            var uploadResult = cloudinary.uploader().upload(data, params);
            var url = (String) uploadResult.get("secure_url");
            return new StorageResult(publicId, url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload bytes to Cloudinary", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            cloudinary.uploader().destroy(key, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    @Override
    public String getPresignedUrl(String key) {
        return cloudinary.url().secure(true).generate(key);
    }
}
