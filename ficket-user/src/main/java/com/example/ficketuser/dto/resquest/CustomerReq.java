package com.example.ficketuser.dto.resquest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
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
