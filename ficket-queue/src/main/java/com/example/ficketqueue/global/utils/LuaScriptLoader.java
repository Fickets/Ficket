package com.example.ficketqueue.global.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LuaScriptLoader {

    public static String loadScript(String scriptPath) {
        try {
            Resource resource = new ClassPathResource(scriptPath);
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new RuntimeException("Lua 스크립트를 로드할 수 없습니다: " + scriptPath, e);
        }
    }
}
