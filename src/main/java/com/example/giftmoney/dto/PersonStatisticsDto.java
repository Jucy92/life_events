package com.example.giftmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonStatisticsDto {

    private String giverName;
    private String relation;
    private BigDecimal receivedTotal;
    private Long receivedCount;
    private BigDecimal sentTotal;
    private Long sentCount;
    private BigDecimal balance;
    private LocalDate lastEventDate;
    private String lastEventType;

}
