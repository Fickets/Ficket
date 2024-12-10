package com.example.ficketuser.mapper;

import com.example.ficketuser.dto.client.TicketInfoDto;
import com.example.ficketuser.dto.response.MySeatInfo;
import com.example.ficketuser.dto.response.MyTicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    // TicketInfoDto -> MySeatInfo 변환
    @Mapping(target = "seatGrade", source = "seatGrade")
    @Mapping(target = "seatRow", source = "seatRow")
    @Mapping(target = "seatCol", source = "seatCol")
    MySeatInfo toMySeatInfo(TicketInfoDto ticketInfoDto);

    // TicketInfoDto -> MyTicketResponse 변환 (그룹화 전)
    @Mapping(target = "mySeatInfoList", ignore = true) // mySeatInfoList는 그룹화 후 별도로 매핑
    MyTicketResponse toMyTicketResponse(TicketInfoDto ticketInfoDto);
}
