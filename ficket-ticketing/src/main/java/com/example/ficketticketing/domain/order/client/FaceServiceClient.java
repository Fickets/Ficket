package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.FaceApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "face-service")
public interface FaceServiceClient {


//    @RequestLine("POST /upload")
//    @Headers("Content-Type: multipart/form-data")
//    FaceApiResponse uploadFace(
//            @Param("file") MultipartFile file,                // 파일 부분
//            @Param("ticket_id") Long ticketId,              // ticket_id 필드
//            @Param("event_schedule_id") Long eventScheduleId // event_schedule_id 필드
//    );

    @PostMapping(value = "/api/v1/faces/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FaceApiResponse uploadFace(
            @RequestPart("file") MultipartFile file,                // 파일 부분
            @RequestPart("ticket_id") Long ticketId,              // ticket_id 필드
            @RequestPart("event_schedule_id") Long eventScheduleId // event_schedule_id 필드
    );
}
