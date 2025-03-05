package com.example.ficketevent.global.utils;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;

import java.io.InputStream;

public class XSSSanitizer {
    private static final Policy policy = loadPolicy();

    private static Policy loadPolicy() {
        try (InputStream is = XSSSanitizer.class.getClassLoader().getResourceAsStream("antisamy-tinymce.xml")) {
            return (is != null) ? Policy.getInstance(is) : null;
        } catch (Exception e) {
            throw new RuntimeException("❌ AntiSamy 정책 파일 로드 실패", e);
        }
    }

    public static String sanitize(String input) {
        if (input == null || input.isBlank() || policy == null) {
            return input;
        }
        try {
            AntiSamy antiSamy = new AntiSamy(policy);
            CleanResults cleanResults = antiSamy.scan(input);
            return cleanResults.getCleanHTML();
        } catch (Exception e) {
            return input; // 필터링 실패 시 원본 유지
        }
    }
}
