package com.example.ficketsearch.global.config.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static com.example.ficketsearch.global.config.awsS3.AwsConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {

    private final AmazonS3Client amazonS3Client;

    public String downloadFile(String s3Url) {
        try {
            // S3에서 객체 가져오기
            S3Object s3Object = amazonS3Client.getObject(CONTENT_BUCKET_NAME, FileUtils.extractFileKey(s3Url));
            InputStream inputStream = s3Object.getObjectContent();

            String rootDir = System.getProperty("user.dir"); // 현재 작업 디렉토리
            String localFilePath = rootDir + File.separator + "downloads" + File.separator + FileUtils.extractFileName(s3Url); // 다운로드 디렉토리에 파일 저장
            File file = new File(localFilePath);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            log.info("File downloaded successfully to: {}", rootDir);
            return localFilePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

}
