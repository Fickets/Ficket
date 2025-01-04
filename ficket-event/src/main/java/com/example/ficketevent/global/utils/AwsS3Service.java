package com.example.ficketevent.global.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.ficketevent.global.config.awsS3.AwsConstants;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.example.ficketevent.global.config.awsS3.AwsConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3Client amazonS3Client;

    public String uploadPosterOriginImage(MultipartFile file) {
        return upload(file, ORIGINAL_BUCKET_NAME, ORIGIN_POSTER_FOLDER);
    }

    public String uploadBannerOriginImage(MultipartFile file) {
        return upload(file, ORIGINAL_BUCKET_NAME, ORIGIN_BANNER_FOLDER);
    }

    public String upload(MultipartFile file, String bucket, String folder) {

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        String originalFileName = file.getOriginalFilename();

        // 파일 형식 체크
        FileUtils.checkFileFormat(originalFileName);

        // 파일 생성
        String key = FileUtils.makeFileName(originalFileName, folder);

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucket, key, inputStream, objectMetadata));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUNT);
        }

        String storedFileUrl = amazonS3Client.getUrl(bucket, key).toString();

        return storedFileUrl;
    }

    public String getResizedImageUrl(String resizedBucket, String folder, String fileName) {
        String key = folder + "/" + fileName;
        return amazonS3Client.getUrl(resizedBucket, key).toString();
    }

    public void deletePosterImage(String originFileName) {
        delete(ORIGIN_POSTER_FOLDER + "/" + originFileName, ORIGINAL_BUCKET_NAME);
    }

    public void deleteResizedPosterImage(String posterMobileFileName, String posterPcFileName, String posterPcMain1FileName, String posterPcMain2FileName) {
        delete(RESIZED_MOBILE_POSTER + "/" + posterMobileFileName, RESIZED_BUCKET_NAME);
        delete(RESIZED_PC_POSTER + "/" + posterPcFileName, RESIZED_BUCKET_NAME);
        delete(RESIZED_PC_POSTER_MAIN1 + "/" + posterPcMain1FileName, RESIZED_BUCKET_NAME);
        delete(RESIZED_PC_POSTER_MAIN2 + "/" + posterPcMain2FileName, RESIZED_BUCKET_NAME);
    }

    public void deleteBannerImage(String originFileName) {
        delete(ORIGIN_BANNER_FOLDER + "/" + originFileName, ORIGINAL_BUCKET_NAME);
    }

    public void deleteResizedBannerImage(String bannerMobileFileName, String bannerPcFileName) {
        delete(RESIZED_MOBILE_BANNER + "/" + bannerMobileFileName, RESIZED_BUCKET_NAME);
        delete(RESIZED_PC_BANNER + "/" + bannerPcFileName, RESIZED_BUCKET_NAME);
    }

    public void delete(String filePath, String bucket) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, filePath));
    }

    public String uploadEventListInfoFile(File file) {

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/csv");
        objectMetadata.setContentEncoding("UTF-8"); // UTF-8 인코딩 명시
        objectMetadata.setContentLength(file.length());

        String originalFileName = file.getName();

        int index = originalFileName.lastIndexOf(".");
        String ext = originalFileName.substring(index + 1);

        String key = String.format("%s/events_version_%s.%s",EVENT_INFO_LIST_FOLDER, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")), ext);

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            amazonS3Client.putObject(new PutObjectRequest(CONTENT_BUCKET_NAME, key, fileInputStream, objectMetadata));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUNT);
        }

        return amazonS3Client.getUrl(CONTENT_BUCKET_NAME, key).toString();
    }
}
