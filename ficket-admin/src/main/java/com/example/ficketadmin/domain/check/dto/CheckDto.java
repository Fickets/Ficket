package com.example.ficketadmin.domain.check.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckDto {
    private List<String> seatLoc;
    private String name;
    private int birth;
    private Object data;
}
