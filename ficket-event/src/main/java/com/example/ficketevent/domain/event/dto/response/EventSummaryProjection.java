package com.example.ficketevent.domain.event.dto.response;

import java.time.LocalDateTime;

public interface EventSummaryProjection {
    Long getEventId();
    String getTitle();
    String getStageName();
    Long getCompanyId();
    Long getAdminId();
    LocalDateTime getMinEventDate();
    LocalDateTime getMaxEventDate();
}
