package com.example.ficketsearch.global.config.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {

    private final AmazonS3Client amazonS3Client;

    /**
     * S3 파일 다운로드 (재시도 포함)
     *
     * @param s3Url S3 URL (예: s3://bucket-name/path/to/file.csv)
     * @return 로컬 파일 경로
     */
    public String downloadFileWithRetry(String s3Url) {
        int maxRetry = 3;
        int retryCount = 0;

        while (retryCount < maxRetry) {
            try {
                return downloadFile(s3Url);
            } catch (Exception e) {
                retryCount++;
                log.warn("S3 파일 다운로드 재시도 중 ({}/{}): {}", retryCount, maxRetry, e.getMessage());

                if (retryCount >= maxRetry) {
                    throw new RuntimeException("S3 파일 다운로드 실패 (최대 재시도 초과): " + s3Url, e);
                }

                // 재시도 전 잠시 대기
                try {
                    Thread.sleep(1000 * retryCount); // 1초, 2초, 3초 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException("S3 파일 다운로드 실패 (최대 재시도 초과): " + s3Url);
    }

    /**
     * S3 파일 다운로드
     *
     * @param s3Url S3 URL (예: s3://ficket-event-content/index/full/2025/12/09/events.csv)
     * @return 로컬 파일 경로
     */
    public String downloadFile(String s3Url) {
        log.info("S3 파일 다운로드 시작: {}", s3Url);

        // S3 URL 파싱
        S3UrlInfo s3Info = parseS3Url(s3Url);
        log.debug("Bucket: {}, Key: {}, FileName: {}", s3Info.bucket, s3Info.key, s3Info.fileName);

        // 다운로드 디렉토리 생성
        String rootDir = System.getProperty("user.dir");
        String downloadDirPath = rootDir + File.separator + "downloads";
        String localFilePath = downloadDirPath + File.separator + s3Info.fileName;

        File downloadDir = new File(downloadDirPath);
        if (!downloadDir.exists()) {
            boolean isCreated = downloadDir.mkdirs();
            if (isCreated) {
                log.info("다운로드 디렉토리가 생성되었습니다: {}", downloadDirPath);
            } else {
                throw new RuntimeException("다운로드 디렉토리 생성에 실패했습니다: " + downloadDirPath);
            }
        }

        // S3에서 파일 다운로드
        try (S3Object s3Object = amazonS3Client.getObject(s3Info.bucket, s3Info.key);
             S3ObjectInputStream inputStream = s3Object.getObjectContent();
             FileOutputStream outputStream = new FileOutputStream(localFilePath)) {

            log.info("S3 객체 다운로드 중: bucket={}, key={}", s3Info.bucket, s3Info.key);

            byte[] buffer = new byte[8192]; // 버퍼 크기 증가
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            log.info("S3 파일 다운로드 완료: {} ({} bytes)", localFilePath, totalBytes);
            return localFilePath;

        } catch (Exception e) {
            log.error("S3 파일 다운로드 실패 - Bucket: {}, Key: {}, LocalPath: {}",
                    s3Info.bucket, s3Info.key, localFilePath, e);
            throw new RuntimeException("S3에서 파일 다운로드에 실패했습니다: " + s3Url, e);
        }
    }

    /**
     * S3 URL 파싱
     *
     * s3://bucket-name/path/to/file.csv 형식을 파싱하여
     * bucket, key, fileName 추출
     */
    private S3UrlInfo parseS3Url(String s3Url) {
        try {
            // s3:// 제거하고 파싱
            if (!s3Url.startsWith("s3://")) {
                throw new IllegalArgumentException("잘못된 S3 URL 형식입니다. s3://로 시작해야 합니다: " + s3Url);
            }

            URI uri = URI.create(s3Url);
            String bucket = uri.getHost();
            String key = uri.getPath();

            // 앞의 / 제거
            if (key.startsWith("/")) {
                key = key.substring(1);
            }

            // 파일명 추출
            String fileName = key.substring(key.lastIndexOf('/') + 1);

            if (bucket == null || bucket.isEmpty()) {
                throw new IllegalArgumentException("S3 버킷 이름을 추출할 수 없습니다: " + s3Url);
            }

            if (key.isEmpty()) {
                throw new IllegalArgumentException("S3 키를 추출할 수 없습니다: " + s3Url);
            }

            return new S3UrlInfo(bucket, key, fileName);

        } catch (Exception e) {
            log.error("S3 URL 파싱 실패: {}", s3Url, e);
            throw new IllegalArgumentException("S3 URL 파싱에 실패했습니다: " + s3Url, e);
        }
    }

    /**
     * S3 URL 정보를 담는 내부 클래스
     */
    private static class S3UrlInfo {
        final String bucket;
        final String key;
        final String fileName;

        S3UrlInfo(String bucket, String key, String fileName) {
            this.bucket = bucket;
            this.key = key;
            this.fileName = fileName;
        }
    }
}