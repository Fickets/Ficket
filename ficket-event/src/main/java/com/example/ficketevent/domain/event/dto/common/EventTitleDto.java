package com.example.ficketevent.domain.event.dto.common;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventTitleDto {
    private Long eventId;
    private Long companyId;
    private String title;
}
