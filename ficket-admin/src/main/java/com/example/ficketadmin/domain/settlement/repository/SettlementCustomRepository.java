package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.dto.common.EventTitleDto;
import com.example.ficketadmin.domain.settlement.dto.request.SettlementReq;
import com.example.ficketadmin.domain.settlement.dto.response.SettlementRecordDto;
import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementCustomRepository {

    List<SettlementRecordDto> getTotalSettlementPage(SettlementReq req, List<EventTitleDto> eventIds);

    List<Settlement> findSettlementByEventId(Long eventId);

    @Query("SELECT s FROM Settlement s WHERE s.settlementRecord.eventId = :eventId AND s.settlementStatus != :status")
    List<Settlement> findSettlementByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") SettlementStatus settlementStatus);


}
