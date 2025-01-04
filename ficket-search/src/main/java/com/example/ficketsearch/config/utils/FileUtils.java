package com.example.ficketsearch.config.utils;

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

}