package com.example.ficketadmin.domain.settlement.controller;


import com.example.ficketadmin.domain.settlement.dto.request.OrderSimpleDto;
import com.example.ficketadmin.domain.settlement.dto.request.SettlementReq;
import com.example.ficketadmin.domain.settlement.dto.response.PageResponse;
import com.example.ficketadmin.domain.settlement.dto.response.SettlementRecordDto;
import com.example.ficketadmin.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;


    @PostMapping("/create")
    ResponseEntity<Void> createSettlement(OrderSimpleDto orderSimpleDto){
        settlementService.createSettlement(orderSimpleDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/create-total")
    int createTotalSettlement(@RequestParam Long eventId){
         settlementService.createTotalSettlement(eventId);
        return 200;
    }

    @GetMapping()
    ResponseEntity<PageResponse<SettlementRecordDto>> getSettlementList(SettlementReq settlementReq, Pageable pageable){
        PageResponse<SettlementRecordDto> res = settlementService.getTotalSettlementList(settlementReq, pageable);
        return ResponseEntity.ok(res);
    }
}
