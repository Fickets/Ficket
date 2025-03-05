package com.example.ficketadmin.domain.settlement.mapper;


import com.example.ficketadmin.domain.settlement.entity.SettlementRecord;
import com.example.ficketadmin.domain.settlement.entity.SettlementRecordTemp;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SettlementRecordMapper {

    SettlementRecord toSettlementRecordByTemp(SettlementRecordTemp settlementRecordTemp);
}
