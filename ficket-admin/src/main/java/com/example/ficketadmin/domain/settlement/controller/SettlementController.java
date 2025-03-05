package com.example.ficketadmin.domain.settlement.controller;


import com.example.ficketadmin.domain.settlement.dto.request.OrderSimpleDto;
import com.example.ficketadmin.domain.settlement.dto.request.SettlementReq;
import com.example.ficketadmin.domain.settlement.dto.response.PageResponse;
import com.example.ficketadmin.domain.settlement.dto.response.SettlementRecordDto;
import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;


    @PostMapping("/create")
    ResponseEntity<Void> createSettlement(@RequestBody OrderSimpleDto orderSimpleDto) {
        settlementService.createSettlement(orderSimpleDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/create-total")
    int createTotalSettlement(@RequestParam Long eventId) {
        settlementService.createTotalSettlement(eventId);
        return 200;
    }

    @GetMapping()
    ResponseEntity<PageResponse<SettlementRecordDto>> getSettlementRecordPage(SettlementReq settlementReq, Pageable pageable) {
        PageResponse<SettlementRecordDto> res = settlementService.getTotalSettlementList(settlementReq, pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/list/{eventId}")
    ResponseEntity<List<Settlement>> getSettlementList(@PathVariable(name = "eventId") Long eventId) {
        List<Settlement> res = settlementService.getSettlementList(eventId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/clear/{eventId}")
    ResponseEntity<Void> settlementClear(@PathVariable(name = "eventId") Long eventId) {
        settlementService.settlementClear(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/refund")
    ResponseEntity<Void> refundSettlement(@RequestParam(name = "orderId") Long orderId, @RequestParam(name = "ticketId") Long ticketId, @RequestParam(name = "refund") BigDecimal refund) {
        settlementService.refundSettlement(orderId, ticketId, refund);
        return ResponseEntity.noContent().build();
    }
}
