package com.example.ficketticketing.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentSseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String paymentId) {
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(paymentId, emitter);

        emitter.onCompletion(() -> emitters.remove(paymentId));
        emitter.onTimeout(() -> emitters.remove(paymentId));
        emitter.onError((e) -> emitters.remove(paymentId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connection established"));
        } catch (IOException e) {
            emitters.remove(paymentId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void notifyPaymentStatus(String paymentId, String status) {
        SseEmitter emitter = emitters.get(paymentId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(Map.of("paymentId", paymentId, "status", status)));
                emitter.complete();
            } catch (IOException e) {
                emitters.remove(paymentId);
            }
        }
    }
}
