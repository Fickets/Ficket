package com.example.ficketevent.global.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.example.ficketevent.global.config.awsS3.AwsConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3Client amazonS3Client;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 이미지 업로드 ====================

    public String uploadPosterOriginImage(MultipartFile file) {
        return uploadImage(file, ORIGINAL_BUCKET_NAME, ORIGIN_POSTER_FOLDER);
    }

    public String uploadBannerOriginImage(MultipartFile file) {
        return uploadImage(file, ORIGINAL_BUCKET_NAME, ORIGIN_BANNER_FOLDER);
    }

    public String uploadEventInfoImage(MultipartFile file) {
        return uploadImage(file, ORIGINAL_BUCKET_NAME, CONTENT_BUCKET_NAME);
    }

    private String uploadImage(MultipartFile file, String bucket, String folder) {
        String originalFileName = file.getOriginalFilename();
        FileUtils.checkFileFormat(originalFileName);

        String key = FileUtils.makeFileName(originalFileName, folder);

        ObjectMetadata metadata = createImageMetadata(file);

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucket, key, inputStream, metadata));
            return amazonS3Client.getUrl(bucket, key).toString();
        } catch (IOException e) {
            log.error("이미지 업로드 실패: bucket={}, key={}", bucket, key, e);
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUNT);
        }
    }

    private ObjectMetadata createImageMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        return metadata;
    }

    // ==================== CSV 업로드 ====================

    /**
     * CSV InputStream을 S3에 업로드
     * 파일명: events_2025-12-04_a1b2c3d4.csv
     */
    public String uploadEventCsv(InputStream csvStream, String fileName, long contentLength) {
        String key = EVENT_INFO_LIST_FOLDER + "/" + fileName;

        ObjectMetadata metadata = createCsvMetadata(contentLength);

        try {
            amazonS3Client.putObject(new PutObjectRequest(CONTENT_BUCKET_NAME, key, csvStream, metadata));
            String url = amazonS3Client.getUrl(CONTENT_BUCKET_NAME, key).toString();
            log.info("CSV 업로드 성공: {}", key);
            return url;
        } catch (Exception e) {
            log.error("CSV 업로드 실패: key={}", key, e);
            throw new RuntimeException("CSV 업로드 실패", e);
        }
    }

    private ObjectMetadata createCsvMetadata(long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/csv");
        metadata.setContentEncoding("UTF-8");
        metadata.setContentLength(contentLength);
        return metadata;
    }

    // ==================== 이미지 조회 ====================

    public String getResizedImageUrl(String resizedBucket, String folder, String fileName) {
        String key = folder + "/" + fileName;
        return amazonS3Client.getUrl(resizedBucket, key).toString();
    }

    // ==================== 이미지 삭제 ====================

    public void deletePosterImage(String originFileName) {
        deleteFile(ORIGINAL_BUCKET_NAME, ORIGIN_POSTER_FOLDER + "/" + originFileName);
    }

    public void deleteResizedPosterImage(String posterMobileFileName, String posterPcFileName,
                                         String posterPcMain1FileName, String posterPcMain2FileName) {
        deleteFile(RESIZED_BUCKET_NAME, RESIZED_MOBILE_POSTER + "/" + posterMobileFileName);
        deleteFile(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER + "/" + posterPcFileName);
        deleteFile(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN1 + "/" + posterPcMain1FileName);
        deleteFile(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN2 + "/" + posterPcMain2FileName);
    }

    public void deleteBannerImage(String originFileName) {
        deleteFile(ORIGINAL_BUCKET_NAME, ORIGIN_BANNER_FOLDER + "/" + originFileName);
    }

    public void deleteResizedBannerImage(String bannerMobileFileName, String bannerPcFileName) {
        deleteFile(RESIZED_BUCKET_NAME, RESIZED_MOBILE_BANNER + "/" + bannerMobileFileName);
        deleteFile(RESIZED_BUCKET_NAME, RESIZED_PC_BANNER + "/" + bannerPcFileName);
    }

    private void deleteFile(String bucket, String key) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, key));
            log.info("파일 삭제 성공: bucket={}, key={}", bucket, key);
        } catch (Exception e) {
            log.error("파일 삭제 실패: bucket={}, key={}", bucket, key, e);
        }
    }
}