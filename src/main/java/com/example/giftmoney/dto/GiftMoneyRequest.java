package com.example.giftmoney.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GiftMoneyRequest {

    @NotNull(message = "행사 날짜는 필수입니다")
    private LocalDate eventDate;

    @NotBlank(message = "행사 유형은 필수입니다")
    @Size(max = 50, message = "행사 유형은 50자 이내여야 합니다")
    private String eventType;

    @NotBlank(message = "거래 유형은 필수입니다")
    @Pattern(regexp = "^(RECEIVED|SENT)$", message = "거래 유형은 RECEIVED 또는 SENT만 가능합니다")
    private String transactionType = "RECEIVED";

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이내여야 합니다")
    private String name;

    @Size(max = 50, message = "관계는 50자 이내여야 합니다")
    private String relation;

    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 양수여야 합니다")
    private BigDecimal amount;

    @Pattern(regexp = "^$|^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String contact;

    private String memo;

}
