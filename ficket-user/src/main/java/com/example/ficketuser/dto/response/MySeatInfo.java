package com.example.ficketuser.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySeatInfo {
    private String seatGrade;
    private String seatRow;
    private String seatCol;
}
