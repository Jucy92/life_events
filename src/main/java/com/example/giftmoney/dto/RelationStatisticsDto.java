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
public class RelationStatisticsDto {

    private String relation;
    private BigDecimal receivedTotal;
    private Long receivedCount;
    private BigDecimal sentTotal;
    private Long sentCount;
    private BigDecimal averageReceived;
    private BigDecimal averageSent;

}
