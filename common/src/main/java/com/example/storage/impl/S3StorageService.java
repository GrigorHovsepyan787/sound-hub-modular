package com.example.storage.impl;

import com.example.properties.S3Properties;
import com.example.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@RequiredArgsConstructor
@Service
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String upload(MultipartFile multipartFile, String folder) {
        try {
            long timestamp = System.currentTimeMillis();
            String fileName = timestamp + "-" + multipartFile.getOriginalFilename();
            String key = s3Properties.getFolders().get(folder) + "/" + fileName;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(multipartFile.getBytes())
            );
            return String.join("","https://", s3Properties.getBucket(),
                    ".s3.", s3Properties.getRegion(), ".amazonaws.com/", key);
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
        }
        return null;
    }
}