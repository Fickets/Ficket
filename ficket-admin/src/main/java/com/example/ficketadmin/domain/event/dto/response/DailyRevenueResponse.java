package com.example.ficketadmin.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueResponse {

    private Date date;
    private BigDecimal revenue;

}
