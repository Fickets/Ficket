package com.example.ficketadmin.domain.settlement.mapper;


import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.entity.SettlementTemp;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SettlementMapper {

    Settlement toSettlementByTemp(SettlementTemp settlementTemp);

}
