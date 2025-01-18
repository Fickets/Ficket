package com.example.ficketsearch.global.config.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
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

    public String downloadFileWithRetry(String s3Url) {
        int maxRetry = 3;
        int retryCount = 0;
        while (retryCount < maxRetry) {
            try {
                return downloadFile(s3Url);
            } catch (Exception e) {
                retryCount++;
                log.warn("S3 파일 다운로드 재시도 중 ({}/{}): {}", retryCount, maxRetry, e.getMessage());
            }
        }
        throw new RuntimeException("S3 파일 다운로드 실패 (최대 재시도 초과)");
    }

    public String downloadFile(String s3Url) {
        String rootDir = System.getProperty("user.dir"); // 현재 작업 디렉토리
        String downloadDirPath = rootDir + File.separator + "downloads"; // 다운로드 디렉토리 경로
        String localFilePath = downloadDirPath + File.separator + FileUtils.extractFileName(s3Url);

        // downloads 폴더 생성
        File downloadDir = new File(downloadDirPath);
        if (!downloadDir.exists()) {
            boolean isCreated = downloadDir.mkdirs();
            if (isCreated) {
                log.info("Downloads directory created: {}", downloadDirPath);
            } else {
                throw new RuntimeException("Failed to create downloads directory");
            }
        }

        // S3에서 객체 가져오기 및 파일 저장
        try (S3Object s3Object = amazonS3Client.getObject(CONTENT_BUCKET_NAME, EVENT_INFO_LIST_FOLDER + "/" + FileUtils.extractFileName(s3Url));
             S3ObjectInputStream inputStream = s3Object.getObjectContent();
             FileOutputStream outputStream = new FileOutputStream(localFilePath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            log.info("File downloaded successfully to: {}", localFilePath);
            return localFilePath;

        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3. URL: " + s3Url + ", Local Path: " + localFilePath, e);
        }
    }
}
