package com.example.ficketsearch.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaControlService {

    private final KafkaListenerEndpointRegistry registry;

    public void pausePartialIndexing() {
        var container = registry.getListenerContainer("partialIndexingListener");
        if (container != null) {
            container.pause();
        }
    }

    public void resumePartialIndexing() {
        var container = registry.getListenerContainer("partialIndexingListener");
        if (container != null) {
            container.resume();
        }
    }
}
