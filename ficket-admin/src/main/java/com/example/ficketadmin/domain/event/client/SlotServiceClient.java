package com.example.ficketadmin.domain.event.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "queue-service")
public interface SlotServiceClient {

    /**
     * 최대 슬롯 수 설정
     *
     * @param eventId 이벤트 ID
     * @param maxSlot 최대 슬롯 수
     */
    @PostMapping("/api/v1/queues/{eventId}/initialize-slot")
    void setMaxSlot(@PathVariable String eventId, @RequestParam int maxSlot);

    /**
     * 슬롯 제거
     *
     * @param eventId 이벤트 ID
     */
    @DeleteMapping("/api/v1/queues/{eventId}/delete-slot")
    void deleteSlot(@PathVariable String eventId);


}
