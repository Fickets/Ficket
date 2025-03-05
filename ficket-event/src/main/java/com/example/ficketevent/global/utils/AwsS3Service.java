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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.*;
import static com.example.ficketevent.global.config.awsS3.AwsConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3Client amazonS3Client;
    private final ConcurrentHashMap<String, AtomicLong> sequenceMap = new ConcurrentHashMap<>();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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
        objectMetadata.setContentEncoding("UTF-8");
        objectMetadata.setContentLength(file.length());

        // 파일 확장자 추출
        int index = file.getName().lastIndexOf(".");
        String ext = (index > 0) ? file.getName().substring(index + 1) : "csv";

        // 순번 읽기 및 증가
        long fileSequence = getNextSequence();

        // S3에 저장될 파일 키 생성 (날짜 + 순번 기반)
        String key = String.format(
                "%s/%s_%05d.%s", // 예: event_info_list/2025-01-18_00001.csv
                EVENT_INFO_LIST_FOLDER,
                DATE_FORMAT.format(new Date()), // 오늘 날짜
                fileSequence,
                ext
        );
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            amazonS3Client.putObject(new PutObjectRequest(CONTENT_BUCKET_NAME, key, fileInputStream, objectMetadata));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        return amazonS3Client.getUrl(CONTENT_BUCKET_NAME, key).toString();
    }

    private long getNextSequence() {
        String today = DATE_FORMAT.format(new Date());

        // ConcurrentHashMap을 사용해 날짜별 시퀀스를 관리
        sequenceMap.putIfAbsent(today, new AtomicLong(0));
        return sequenceMap.get(today).incrementAndGet();
    }

    public List<String> getFiles() {
        List<String> fileKeys = new ArrayList<>();
        String continuationToken = null;

        // S3 폴더에 있는 모든 파일 리스트 가져오기
        do {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(CONTENT_BUCKET_NAME)
                    .withPrefix(EVENT_INFO_LIST_FOLDER) // 특정 폴더(prefix)만 검색
                    .withContinuationToken(continuationToken);

            ListObjectsV2Result result = amazonS3Client.listObjectsV2(request);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                fileKeys.add(objectSummary.getKey()); // 파일 경로 추가
            }

            continuationToken = result.getNextContinuationToken(); // 더 많은 결과가 있는 경우 처리
        } while (continuationToken != null);

        return fileKeys;
    }

    public void deleteAllFiles() {
        ObjectListing objectListing = amazonS3Client.listObjects(CONTENT_BUCKET_NAME, EVENT_INFO_LIST_FOLDER);

        // 모든 파일 삭제
        while (true) {
            List<KeyVersion> keysToDelete = new ArrayList<>();
            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                keysToDelete.add(new KeyVersion(summary.getKey()));
            }

            if (!keysToDelete.isEmpty()) {
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(CONTENT_BUCKET_NAME)
                        .withKeys(keysToDelete)
                        .withQuiet(true);
                amazonS3Client.deleteObjects(deleteObjectsRequest);
            }

            // 다음 페이지 확인
            if (objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }
}
