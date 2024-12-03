package com.example.ficketevent.domain.event.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long companyId;
    private String companyName;
}
