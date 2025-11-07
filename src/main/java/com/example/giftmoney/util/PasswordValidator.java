package com.example.giftmoney.util;

import java.util.Arrays;
import java.util.List;

/**
 * ⚡ 비밀번호 정책 검증 유틸리티 (보안 강화 2025-11-07)
 *
 * 현재 정책: 최소 8자
 *
 * === 향후 강화된 정책 적용 방법 ===
 * 1. AuthService.java에서 validatePasswordStrength() 메서드 주석 해제
 * 2. RegisterRequest.java에서 @Pattern 어노테이션 주석 해제 (선택사항)
 * 3. 프론트엔드(register.html)에 비밀번호 규칙 안내 추가
 */
public class PasswordValidator {

    // 일반적인 비밀번호 목록 (차단 대상)
    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
        "password", "password123", "12345678", "qwerty123",
        "password1", "123456789", "1234567890", "qwerty",
        "abc123", "password1234", "admin123", "welcome123"
    );

    /**
     * 강화된 비밀번호 정책 검증 (현재 주석 처리됨)
     *
     * 정책:
     * - 최소 8자 이상
     * - 대문자 1자 이상
     * - 소문자 1자 이상
     * - 숫자 1자 이상
     * - 특수문자 1자 이상 (@$!%*?&)
     * - 일반적인 비밀번호 차단
     *
     * @param password 검증할 비밀번호
     * @throws IllegalArgumentException 정책 위반 시
     */
    /*
    public static void validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 항목입니다.");
        }

        // 최소 길이 검증
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        // 복잡도 검증
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");

        if (!hasUpper) {
            throw new IllegalArgumentException("비밀번호는 대문자를 1자 이상 포함해야 합니다.");
        }
        if (!hasLower) {
            throw new IllegalArgumentException("비밀번호는 소문자를 1자 이상 포함해야 합니다.");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("비밀번호는 숫자를 1자 이상 포함해야 합니다.");
        }
        if (!hasSpecial) {
            throw new IllegalArgumentException("비밀번호는 특수문자(@$!%*?&)를 1자 이상 포함해야 합니다.");
        }

        // 일반적인 비밀번호 차단
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new IllegalArgumentException("너무 일반적인 비밀번호는 사용할 수 없습니다.");
        }
    }
    */

    /**
     * 현재 적용 중인 기본 비밀번호 검증 (최소 8자)
     */
    public static void validateBasicPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 항목입니다.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
    }
}
