package com.example.giftmoney.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이내여야 합니다")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

}
