package com.example.giftmoney.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이내여야 합니다")
    private String userId;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이내여야 합니다")
    private String name;

    private Boolean emailVerified = false;

}
