package com.example.giftmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyStatisticsDto {

    private Integer year;
    private BigDecimal receivedTotal;
    private Long receivedCount;
    private BigDecimal sentTotal;
    private Long sentCount;
    private BigDecimal difference;

}
