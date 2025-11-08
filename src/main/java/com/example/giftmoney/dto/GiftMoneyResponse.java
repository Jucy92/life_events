package com.example.giftmoney.dto;

import com.example.giftmoney.domain.entity.GiftMoney;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GiftMoneyResponse {

    private Long id;
    private LocalDate eventDate;
    private String eventType;
    private String transactionType;
    private String name;
    private String relation;
    private BigDecimal amount;
    private String contact;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GiftMoneyResponse from(GiftMoney giftMoney) {
        return GiftMoneyResponse.builder()
                .id(giftMoney.getId())
                .eventDate(giftMoney.getEventDate())
                .eventType(giftMoney.getEventType())
                .transactionType(giftMoney.getTransactionType())
                .name(giftMoney.getName())
                .relation(giftMoney.getRelation())
                .amount(giftMoney.getAmount())
                .contact(giftMoney.getContact())
                .memo(giftMoney.getMemo())
                .createdAt(giftMoney.getCreatedAt())
                .updatedAt(giftMoney.getUpdatedAt())
                .build();
    }

}
