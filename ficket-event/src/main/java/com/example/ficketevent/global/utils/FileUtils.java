package com.example.ficketevent.global.utils;


import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;

import java.util.UUID;

import static com.example.ficketevent.global.config.awsS3.AwsConstants.ORIGINAL_BUCKET_NAME;


public class FileUtils {

    public static void checkFileFormat(String originalFileName) {

        int index;
        try {
            index = originalFileName.lastIndexOf(".");
        } catch (StringIndexOutOfBoundsException e) {
            throw new BusinessException(ErrorCode.IS_NOT_IMAGE);
        }

        String ext = originalFileName.substring(index + 1);
        if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
            throw new BusinessException(ErrorCode.IS_NOT_IMAGE);
        }
    }

    public static String makeFileName(String originalFileName, String folder) {

        int index = originalFileName.lastIndexOf(".");
        String ext = originalFileName.substring(index + 1);

        // 저장할 파일 이름
        String storedFileName = UUID.randomUUID() + "." + ext;

        // 저장할 디렉토리 경로 + 파일 이름
        return folder + "/" + storedFileName;
    }

    // 이미지 파일 이름만 추출(디렉토리까진 추출x)
    public static String extractFileName(String path) {
        int idx = path.lastIndexOf("/");

        return path.substring(idx + 1);
    }

    public static String convertBucket(String url, String bucketName) {
        return url.replaceFirst(ORIGINAL_BUCKET_NAME, bucketName);
    }

    public static String convertFolder(String url, String folder, String newFolder) {
        return url.replaceFirst(folder, newFolder);
    }
}