
package com.example.ficketticketing.global.config.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap_servers}")
    private String BOOTSTRAP_SERVERS;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); //Serialize 방법 지정
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 신뢰성 보장을 위한 추가 설정
        properties.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 복제본에 데이터 전송 완료 시 성공 응답
        properties.put(ProducerConfig.RETRIES_CONFIG, 3); // 메시지 전송 실패 시 재시도 횟수
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500); // 재시도 간격 (500ms)
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 중복 방지 (Idempotent Producer)
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 배치 크기 (16KB)
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 배치 대기 시간 (5ms)

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}