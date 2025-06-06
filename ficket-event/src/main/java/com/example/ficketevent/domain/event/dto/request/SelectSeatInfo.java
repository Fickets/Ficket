package com.example.ficketevent.domain.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@AllArgsConstructor
public class SelectSeatInfo {

    private Long seatMappingId;
    private BigDecimal seatPrice;
    private String seatGrade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectSeatInfo that = (SelectSeatInfo) o;
        return Objects.equals(seatMappingId, that.seatMappingId) &&
                Objects.equals(seatPrice, that.seatPrice) &&
                Objects.equals(seatGrade, that.seatGrade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatMappingId, seatPrice, seatGrade);
    }
}
