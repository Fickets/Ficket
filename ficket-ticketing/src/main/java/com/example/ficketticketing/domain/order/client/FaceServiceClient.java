package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.FaceApiResponse;
import com.example.ficketticketing.domain.order.dto.request.UploadFaceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "face-service")
public interface FaceServiceClient {


    @DeleteMapping("/api/v1/faces/{ticketId}")
    Void deleteFace(@PathVariable Long ticketId);

    @PostMapping(value = "/api/v1/faces/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FaceApiResponse uploadFace(
            @RequestPart("file") MultipartFile file,
            @RequestPart("event_schedule_id") Long eventScheduleId
    );

    @PostMapping(value = "/api/v1/faces/set-relationship", consumes = MediaType.APPLICATION_JSON_VALUE)
    FaceApiResponse settingRelationship(@RequestBody UploadFaceInfo UploadFaceInfo);

    @PostMapping(value = "/api/v1/faces/match", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FaceApiResponse matchFace(
            @RequestPart("file") MultipartFile file,
            @RequestPart("event_schedule_id") Long eventScheduleId
    );
}
