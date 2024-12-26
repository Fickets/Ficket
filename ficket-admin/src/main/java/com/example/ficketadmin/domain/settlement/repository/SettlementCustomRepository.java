package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.dto.common.EventTitleDto;
import com.example.ficketadmin.domain.settlement.dto.request.SettlementReq;
import com.example.ficketadmin.domain.settlement.dto.response.SettlementRecordDto;
import com.example.ficketadmin.domain.settlement.entity.SettlementRecord;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettlementCustomRepository {

    List<SettlementRecordDto> getTotalSettlementPage(SettlementReq req, List<EventTitleDto> eventIds);
}
