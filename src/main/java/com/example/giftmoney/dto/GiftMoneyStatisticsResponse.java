package com.example.giftmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiftMoneyStatisticsResponse {

    // 받은 축의금 통계
    private BigDecimal receivedTotalAmount = BigDecimal.ZERO;
    private Long receivedCount = 0L;
    private Double receivedAvgAmount = 0.0;

    // 보낸 축의금 통계
    private BigDecimal sentTotalAmount = BigDecimal.ZERO;
    private Long sentCount = 0L;
    private Double sentAvgAmount = 0.0;

    // 전체 통계
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Long totalCount = 0L;
    private Double avgAmount = 0.0;

}
