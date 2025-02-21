package com.example.ficketqueue.global.utils;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Objects;

public class WebSocketUrlParser {


    /**
     * WebSocket 세션의 URI에서 이벤트 ID를 추출
     */
    public static String getInfoFromUri(WebSocketSession session) {
        // WebSocketSession에서 URI 추출
        String fullUri = Objects.requireNonNull(session.getHandshakeInfo().getUri()).toString();
        String uri = session.getHandshakeInfo().getUri().getPath(); // URI 경로 추출

        if (uri == null || !uri.contains("/")) {
            throw new IllegalArgumentException("잘못된 URI 형식: " + fullUri);
        }

        String[] parts = uri.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("이벤트 ID를 추출할 수 없음: " + fullUri);
        }

        return parts[parts.length - 1]; // 마지막 부분을 이벤트 ID로 반환
    }

    public static String[] getEventAndScheduleFromUri(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        String[] pathSegments = path.split("/");

        if (pathSegments.length < 4) {
            return null; // "/work-status/{eventId}/{eventScheduleId}" 형태가 아니면 null 반환
        }

        return new String[]{pathSegments[2], pathSegments[3]}; // [eventId, eventScheduleId] 반환
    }
}
