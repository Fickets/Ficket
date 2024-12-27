package com.example.ficketadmin.domain.settlement.dto.common;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EventTitleDto {
    private Long eventId;
    private Long companyId;
    private String title;
    private String companyName;
}
