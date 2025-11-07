package com.example.giftmoney.controller;

import com.example.giftmoney.dto.LoginRequest;
import com.example.giftmoney.dto.LoginResponse;
import com.example.giftmoney.dto.RegisterRequest;
import com.example.giftmoney.dto.UserResponse;
import com.example.giftmoney.service.AuthService;
import com.example.giftmoney.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Long userId) {
        UserResponse response = authService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        try {
            emailService.verifyEmail(token);
            return ResponseEntity.ok(Map.of(
                    "message", "이메일 인증이 완료되었습니다.",
                    "redirect", "/login"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            emailService.sendVerificationEmail(email);
            return ResponseEntity.ok(Map.of(
                    "message", "인증 이메일이 재발송되었습니다."
            ));
        } catch (Exception e) {
            log.error("인증 이메일 재발송 실패: email={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "이메일 발송에 실패했습니다."
            ));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("OTP 발송 요청: email={}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("OTP 발송 실패: 이메일이 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "이메일을 입력해주세요."
            ));
        }

        try {
            emailService.sendOtpCode(email);
            log.info("OTP 발송 성공: email={}", email);
            return ResponseEntity.ok(Map.of(
                    "message", "인증 코드가 이메일로 발송되었습니다."
            ));
        } catch (Exception e) {
            // ⚡ 보안: 스택 트레이스는 로그에만 기록, 사용자에게는 일반 메시지만 전달 (보안 강화 2025-11-07)
            log.error("OTP 발송 실패: email={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "이메일 발송에 실패했습니다."
            ));
        }
    }

    @GetMapping("/check-userid")
    public ResponseEntity<Map<String, Boolean>> checkUserId(@RequestParam String userId) {
        boolean available = authService.isUserIdAvailable(userId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");
        log.info("OTP 인증 요청: email={}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("OTP 인증 실패: 이메일이 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "error", "이메일을 입력해주세요."
            ));
        }

        if (otpCode == null || otpCode.trim().isEmpty()) {
            log.warn("OTP 인증 실패: 인증 코드가 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "error", "인증 코드를 입력해주세요."
            ));
        }

        try {
            boolean verified = emailService.verifyOtpCode(email, otpCode);
            log.info("OTP 인증 성공: email={}, verified={}", email, verified);
            return ResponseEntity.ok(Map.of(
                    "verified", verified,
                    "message", "이메일 인증이 완료되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.warn("OTP 인증 실패: email={}, error={}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            // ⚡ 보안: 스택 트레이스는 로그에만 기록, 사용자에게는 일반 메시지만 전달
            log.error("OTP 인증 처리 중 오류: email={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "verified", false,
                    "error", "인증 처리 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/find-userid")
    public ResponseEntity<Map<String, String>> findUserId(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("아이디 찾기 요청: email={}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("아이디 찾기 실패: 이메일이 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "이메일을 입력해주세요."
            ));
        }

        try {
            String userId = authService.findUserIdByEmail(email);
            log.info("아이디 찾기 성공: email={}, userId={}", email, userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "message", "아이디를 찾았습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.warn("아이디 찾기 실패: email={}, error={}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            // ⚡ 보안: 스택 트레이스는 로그에만 기록
            log.error("아이디 찾기 처리 중 오류: email={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "아이디 찾기 처리 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<Map<String, String>> resetPasswordRequest(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String email = request.get("email");
        log.info("비밀번호 재설정 요청: userId={}, email={}", userId, email);

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("비밀번호 재설정 실패: 아이디가 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "아이디를 입력해주세요."
            ));
        }

        if (email == null || email.trim().isEmpty()) {
            log.warn("비밀번호 재설정 실패: 이메일이 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "이메일을 입력해주세요."
            ));
        }

        try {
            authService.sendPasswordResetOtp(userId, email);
            log.info("비밀번호 재설정 인증 코드 발송 성공: userId={}, email={}", userId, email);
            return ResponseEntity.ok(Map.of(
                    "message", "비밀번호 재설정 인증 코드가 이메일로 발송되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 요청 실패: userId={}, email={}, error={}", userId, email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            // ⚡ 보안: 스택 트레이스는 로그에만 기록
            log.error("비밀번호 재설정 요청 처리 중 오류: userId={}, email={}", userId, email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "비밀번호 재설정 요청 처리 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");
        String newPassword = request.get("newPassword");
        log.info("비밀번호 재설정 요청: email={}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("비밀번호 재설정 실패: 이메일이 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "이메일을 입력해주세요."
            ));
        }

        if (otpCode == null || otpCode.trim().isEmpty()) {
            log.warn("비밀번호 재설정 실패: 인증 코드가 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "인증 코드를 입력해주세요."
            ));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            log.warn("비밀번호 재설정 실패: 새 비밀번호가 비어있음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "새 비밀번호를 입력해주세요."
            ));
        }

        if (newPassword.length() < 8) {
            log.warn("비밀번호 재설정 실패: 비밀번호가 너무 짧음");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "비밀번호는 최소 8자 이상이어야 합니다."
            ));
        }

        try {
            authService.resetPassword(email, otpCode, newPassword);
            log.info("비밀번호 재설정 성공: email={}", email);
            return ResponseEntity.ok(Map.of(
                    "message", "비밀번호가 재설정되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 실패: email={}, error={}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            // ⚡ 보안: 스택 트레이스는 로그에만 기록
            log.error("비밀번호 재설정 처리 중 오류: email={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "비밀번호 재설정 처리 중 오류가 발생했습니다."
            ));
        }
    }

}
