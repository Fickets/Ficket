package com.example.ficketadmin.domain.check.client;

import com.example.ficketadmin.domain.check.dto.FaceApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "face-service")
public interface FaceServiceClient {

    @PostMapping(value = "/api/v1/faces/match", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FaceApiResponse matchFace(
            @RequestPart("file") MultipartFile file,
            @RequestPart("event_schedule_id") Long eventScheduleId
    );
}
