package com.example.ficketevent.global.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

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

    public void deleteBannerImage(String originFileName) {
        delete(ORIGIN_BANNER_FOLDER + "/" + originFileName, ORIGINAL_BUCKET_NAME);
    }

    public void delete(String filePath, String bucket) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, filePath));
    }
}
