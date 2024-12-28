package com.example.ficketadmin.domain.settlement.service;


import com.example.ficketadmin.domain.company.entity.Company;
import com.example.ficketadmin.domain.company.repository.CompanyRepository;
import com.example.ficketadmin.domain.event.client.EventServiceClient;
import com.example.ficketadmin.domain.settlement.dto.common.EventTitleDto;
import com.example.ficketadmin.domain.settlement.dto.request.OrderSimpleDto;
import com.example.ficketadmin.domain.settlement.dto.request.SettlementReq;
import com.example.ficketadmin.domain.settlement.dto.response.PageResponse;
import com.example.ficketadmin.domain.settlement.dto.response.SettlementRecordDto;
import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.entity.SettlementRecord;
import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import com.example.ficketadmin.domain.settlement.repository.SettlementCustomRepository;
import com.example.ficketadmin.domain.settlement.repository.SettlementRecordRepository;
import com.example.ficketadmin.domain.settlement.repository.SettlementRepository;
import com.example.ficketadmin.global.result.error.ErrorCode;
import com.example.ficketadmin.global.result.error.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.example.ficketadmin.global.utils.CircuitBreakerUtils.executeWithCircuitBreaker;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final CompanyRepository companyRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementRecordRepository settlementRecordRepository;
    private final SettlementCustomRepository settlementCustomRepository;

    private final EventServiceClient eventServiceClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Transactional
    public void createSettlement(OrderSimpleDto orderSimpleDto){

        Company company = companyRepository.findByCompanyId(orderSimpleDto.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        // 서비스료 비율
        BigDecimal benefit = company.getMembership().getBenefit();
        // 거래가액
        BigDecimal transactionAmount = orderSimpleDto.getOrderPrice().subtract(BigDecimal.valueOf(2000L));
        // VAT율 (10%)
        BigDecimal vatRate = BigDecimal.valueOf(0.1);
        // 공급가액 계산
        BigDecimal supplyAmount = transactionAmount.divide(BigDecimal.valueOf(1).add(vatRate), 2, RoundingMode.HALF_UP);
        // VAT 계산
        BigDecimal vat = supplyAmount.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        // 서비스료 계선
        BigDecimal serviceFee = transactionAmount
                .multiply(benefit.divide(BigDecimal.valueOf(100), 5, RoundingMode.HALF_UP)) // 비율 계산
                .setScale(0, RoundingMode.DOWN); // 소수점 제거 (버림)
        // 정산금액
        BigDecimal settlementValue = transactionAmount.subtract(serviceFee);

        SettlementRecord settlementRecord = settlementRecordRepository.findByEventId(orderSimpleDto.getEventId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_SETTMENT_RECORD));

        Settlement settlement = Settlement.builder()
                .orderId(orderSimpleDto.getOrderId())
                .supplyValue(transactionAmount)
                .netSupplyAmount(supplyAmount)
                .vat(vat)
                .serviceFee(serviceFee)
                .settlementValue(settlementValue)
//                .settlementRecord(settlementRecord)
                .settlementStatus(SettlementStatus.UNSETTLED)
                .orderId(orderSimpleDto.getOrderId())
                .build();
        settlementRepository.save(settlement);

        if(settlementRecord.getSettlementStatus().equals(SettlementStatus.SETTLEMENT)){
            settlementRecord.setSettlementStatus(SettlementStatus.PARTIAL_SETTLEMENT);
        }
        // 정산
        settlementRecord.setTotalSettlementValue(settlementRecord.getTotalSettlementValue().add(settlementValue));
        // 공급
        settlementRecord.setTotalNetSupplyAmount(settlementRecord.getTotalNetSupplyAmount().add(supplyAmount));
        // 거래가액
        settlementRecord.setTotalSupplyAmount(settlementRecord.getTotalSupplyAmount().add(supplyAmount));
        // 서비스료
        settlementRecord.setTotalServiceFee(settlementRecord.getTotalServiceFee().add(serviceFee));
        // 세금
        settlementRecord.setTotalVat(settlementRecord.getTotalVat().add(vat));
        settlementRecordRepository.save(settlementRecord);
    }

    public void createTotalSettlement(Long eventId){
        SettlementRecord settlementRecord = SettlementRecord.builder()
                .eventId(eventId)
                .totalSettlementValue(BigDecimal.ZERO)
                .totalServiceFee(BigDecimal.ZERO)
                .totalVat(BigDecimal.ZERO)
                .totalSupplyAmount(BigDecimal.ZERO)
                .totalRefundValue(BigDecimal.ZERO)
                .totalNetSupplyAmount(BigDecimal.ZERO)
                .settlementStatus(SettlementStatus.UNSETTLED)
                .build();
        settlementRecordRepository.save(settlementRecord);
    }

    public PageResponse<SettlementRecordDto> getTotalSettlementList(SettlementReq settlementReq, Pageable pageable){
        System.out.println("TEST " + settlementReq.toString());
        if(settlementReq.getEventName() == null){
            settlementReq.setEventName("");
        }
        List<EventTitleDto> eventIds = getGetEventIdByTitleCircuitBreaker(settlementReq);
        for (EventTitleDto eventId : eventIds) {
            Company company = companyRepository.findByCompanyId(eventId.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
            eventId.setCompanyName(company.getCompanyName());
        }

        List<SettlementRecordDto> results =  settlementCustomRepository.getTotalSettlementPage(settlementReq, eventIds);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<SettlementRecordDto> pagedResults = results.subList(start, end);

        return new PageResponse<>(
                pagedResults,
                pageable. getPageNumber(),
                pageable.getPageSize(),
                results.size(),
                (int) Math.ceil((double) results.size() / pageable.getPageSize())
        );
    }

    @NotNull
    private List<EventTitleDto> getGetEventIdByTitleCircuitBreaker(SettlementReq settlementReq) {
        return executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getEventIdByTitleCircuitBreaker",
                () -> eventServiceClient.getEventIds(settlementReq.getEventName())
        );
    }

    public List<Settlement> getSettlementList(Long eventId) {
        return settlementCustomRepository.findSettlementByEventId(eventId);
    }

    @Transactional
    public void settlementClear(Long eventId){
        SettlementRecord record = settlementRecordRepository.findByEventId(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_SETTMENT_RECORD));
        List<Settlement> settlements = settlementCustomRepository.findSettlementByEventIdAndStatus(eventId, SettlementStatus.SETTLEMENT);
        for (Settlement settlement : settlements) {
            record.setTotalNetSupplyAmount(record.getTotalNetSupplyAmount().add(settlement.getNetSupplyAmount()));
            record.setTotalVat(record.getTotalVat().add(settlement.getVat()));
            record.setTotalSupplyAmount(record.getTotalSupplyAmount().add(settlement.getSupplyValue()));
            record.setTotalServiceFee(record.getTotalServiceFee().add(settlement.getServiceFee()));
            record.setTotalRefundValue(record.getTotalRefundValue().add(settlement.getRefundValue()));
            record.setTotalSettlementValue(record.getTotalSettlementValue().add(settlement.getSettlementValue()));
            BigDecimal updatedSettlementValue = record.getTotalSettlementValue().subtract(settlement.getRefundValue());
            record.setTotalSettlementValue(updatedSettlementValue);
            settlement.setSettlementStatus(SettlementStatus.SETTLEMENT);
        }
        record.setSettlementStatus(SettlementStatus.SETTLEMENT);
        settlementRecordRepository.save(record);
    }

    @Transactional
    public void refundSettlement(Long orderId, BigDecimal refund){
        Settlement settlement = settlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_SETTMENT_RECORD));
        SettlementRecord record =  settlement.getSettlementRecord();

        settlement.setRefundValue(refund);
        settlement.setSettlementValue(settlement.getSettlementValue().subtract(refund));

        record.setTotalRefundValue(record.getTotalRefundValue().add(refund));
        record.setTotalSettlementValue(record.getTotalSettlementValue().subtract(refund));
        record.setTotalServiceFee(record.getTotalServiceFee().subtract(settlement.getServiceFee()));

        settlement.setServiceFee(BigDecimal.ZERO);
    }
}
