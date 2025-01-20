package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.queue.enums.WorkStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

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
    public Mono<Void> notifyUser(String userId, WorkStatus workStatus) {
        WebSocketSession session = activeSessions.get(userId);

        if (session != null && session.isOpen()) {
            WebSocketMessage message = session.textMessage(workStatus.toString());
            log.info("사용자 {}에게 메시지 전송: {}", userId, workStatus);
            return session.send(Mono.just(message)).then();
        } else {
            log.warn("사용자 {}의 WebSocket 세션이 유효하지 않습니다.", userId);
            return Mono.empty();
        }
    }

    public boolean isUserSessionExists(String userId) {
        return activeSessions.containsKey(userId);
    }

    public void removeAndCreateNewSession(String userId, WebSocketSession session) {
        WebSocketSession existingSession = activeSessions.get(userId);
        if (existingSession != null) {
            existingSession.close().subscribe(); // 기존 세션 닫기 (Reactive 방식)
        }

        // 새로운 세션으로 대체
        activeSessions.put(userId, session);
    }
}
