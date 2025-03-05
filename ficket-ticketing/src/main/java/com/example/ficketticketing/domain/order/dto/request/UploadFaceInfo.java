package com.example.ficketticketing.domain.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFaceInfo {
    private Long faceId;
    private String faceImgUrl;
    private Long ticketId;
    private Long eventScheduleId;
}
