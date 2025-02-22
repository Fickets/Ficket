package com.example.ficketuser.domain.dto.resquest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CustomerReq {
    private Long userId;
    private String userName;
    private LocalDate startDate;
    private LocalDate endDate;

}
