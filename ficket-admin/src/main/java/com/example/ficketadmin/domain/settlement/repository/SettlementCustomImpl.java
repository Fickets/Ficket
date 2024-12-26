package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.dto.common.EventTitleDto;
import com.example.ficketadmin.domain.settlement.dto.request.SettlementReq;
import com.example.ficketadmin.domain.settlement.dto.response.QSettlementRecordDto;
import com.example.ficketadmin.domain.settlement.dto.response.SettlementRecordDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.ficketadmin.domain.settlement.entity.QSettlementRecord.settlementRecord;

@RequiredArgsConstructor
@Repository
public class SettlementCustomImpl implements SettlementCustomRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<SettlementRecordDto> getTotalSettlementPage(SettlementReq req, List<EventTitleDto> eventIds) {
        List<Long> ids = eventIds.stream()
                .map(EventTitleDto::getEventId)
                .toList();

        List<SettlementRecordDto> settlementRecords =  queryFactory.select(new QSettlementRecordDto(
                settlementRecord.eventId,
                settlementRecord.createdAt,
                settlementRecord.lastModifiedAt,
                settlementRecord.totalNetSupplyAmount,
                settlementRecord.totalServiceFee,
                settlementRecord.totalSettlementValue,
                settlementRecord.settlementStatus
                ))
                .from(settlementRecord)
                .where(
                        inEventIds(ids),
                        req.getSettlementStatus() != null?settlementRecord.settlementStatus.eq(req.getSettlementStatus()): null,
                        req.getStartDate() != null ? settlementRecord.createdAt.goe(req.getStartDate().atStartOfDay()) : null,
                        req.getEndDate() != null ? settlementRecord.createdAt.loe(req.getEndDate().atTime(23, 59, 59)) : null
                ).fetch();

        // 2. eventIds에서 eventId와 title을 map으로 변환 (eventId -> title)
        Map<Long, String> eventIdToTitleMap = eventIds.stream()
                .collect(Collectors.toMap(EventTitleDto::getEventId, EventTitleDto::getTitle));

        // 3. SettlementRecord 데이터를 SettlementRecordDto로 변환하고 title을 추가
        List<SettlementRecordDto> result = settlementRecords.stream()
                .map(settlementRecord -> {
                    // eventId에 해당하는 title을 찾아서 SettlementRecordDto에 추가
                    String title = eventIdToTitleMap.get(settlementRecord.getEventId());
                    return new SettlementRecordDto(
                            settlementRecord.getEventId(),
                            settlementRecord.getCreatedAt(),
                            settlementRecord.getLastModifiedAt(),
                            settlementRecord.getTotalNetSupplyAmount(),
                            settlementRecord.getTotalServiceFee(),
                            settlementRecord.getTotalSettlementValue(),
                            settlementRecord.getSettlementStatus(),
                            title // title을 포함
                    );
                })
                .collect(Collectors.toList());

        return result;
    }

    private BooleanExpression inEventIds(List<Long> eventIds) {
        return (eventIds == null || eventIds.isEmpty())
                ? null // 조건 없이 모든 것을 조회
                : settlementRecord.eventId.in(eventIds); // 리스트가 존재할 때만 조건 적용
    }
}
