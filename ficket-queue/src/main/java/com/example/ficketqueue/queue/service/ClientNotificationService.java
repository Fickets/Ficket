package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.queue.enums.WorkStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    // WebSocket 세션 관리 (userId -> WebSocketSession)
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * WebSocket 세션 등록
     */
    public void registerSession(String userId, WebSocketSession session) {
        activeSessions.put(userId, session);
    }

    /**
     * WebSocket 세션 해제
     */
    public void removeSession(String userId) {
        activeSessions.remove(userId);
    }

    /**
     * 사용자에게 WebSocket 메시지 전송
     */
    public void notifyUser(String userId, WorkStatus workStatus) {
        WebSocketSession session = activeSessions.get(userId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(workStatus.toString()));
                log.info("사용자 {}에게 메시지 전송: {}", userId, workStatus);
            } catch (IOException e) {
                log.error("사용자 {}에게 메시지 전송 중 오류 발생", userId, e);
            }
        } else {
            log.warn("사용자 {}의 WebSocket 세션이 유효하지 않습니다.", userId);
        }
    }
}
