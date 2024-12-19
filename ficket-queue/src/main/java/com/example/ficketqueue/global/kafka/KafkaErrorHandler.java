package com.example.ficketqueue.global.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KafkaErrorHandler implements CommonErrorHandler {

    /**
     * 단일 예외 처리
     * - 메시지 처리 중 예외가 발생하면 호출됩니다.
     */
    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
        log.error("Kafka 메시지 처리 중 예외 발생: {}", thrownException.getMessage(), thrownException);

        // 필요에 따라 알림 전송 또는 재시도 로직 추가
        // 예: Dead Letter Queue (DLQ)에 메시지 전송
    }

    /**
     * 예외 발생 후 남은 메시지 처리
     * - 여러 메시지를 처리하는 도중 예외가 발생한 경우 남은 메시지들을 처리하지 못할 때 호출됩니다.
     */
    @Override
    public void handleRemaining(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("Kafka 메시지 처리 중 예외 발생. 남은 메시지 처리 불가. 총 {}건 메시지", records.size(), thrownException);

        // 로그로 남은 메시지 내용 출력
        for (ConsumerRecord<?, ?> record : records) {
            log.error("남은 메시지: Topic={}, Partition={}, Offset={}, Key={}, Value={}",
                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
        }

        // 필요에 따라 남은 메시지 처리 로직 추가 (예: DLQ로 전송)
    }
}
