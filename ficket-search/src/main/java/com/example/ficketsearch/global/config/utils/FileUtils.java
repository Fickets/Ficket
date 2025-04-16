package com.example.ficketsearch.global.config.utils;

import java.io.File;
import java.net.URL;

public class FileUtils {

    public static String extractFileKey(String path) {
        try {
            URL url = new URL(path);
            return url.getPath().substring(1); // 버킷 경로부터 추출
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + path, e);
        }
    }

    public static String extractFileName(String path) {
        try {
            int idx = path.lastIndexOf("/");
            return path.substring(idx + 1);
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + path, e);
        }
    }


    public static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Exception ignored) {
            // 삭제 실패는 무시
        }
    }



}