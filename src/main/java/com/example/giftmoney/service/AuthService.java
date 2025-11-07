package com.example.giftmoney.service;

import com.example.giftmoney.domain.entity.User;
import com.example.giftmoney.dto.LoginRequest;
import com.example.giftmoney.dto.LoginResponse;
import com.example.giftmoney.dto.RegisterRequest;
import com.example.giftmoney.dto.UserResponse;
import com.example.giftmoney.repository.UserRepository;
import com.example.giftmoney.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        if (!Boolean.TRUE.equals(request.getEmailVerified())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다");
        }

        // ⚡ 비밀번호 정책 강화 (향후 활성화 가능)
        // 활성화 방법:
        // 1. 아래 주석 해제
        // 2. util/PasswordValidator.java에서 validatePasswordStrength() 주석 해제
        // 3. 프론트엔드(register.html)에 비밀번호 규칙 안내 추가
        // PasswordValidator.validatePasswordStrength(request.getPassword());

        User user = new User();
        user.setUserId(request.getUserId());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmailVerified(true);

        User saved = userRepository.save(user);

        // JWT 토큰 생성하여 바로 로그인 처리
        String token = tokenProvider.createToken(saved.getId(), saved.getUserId());

        return LoginResponse.builder()
                .token(token)
                .user(UserResponse.from(saved))
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다");
        }

        // 이메일 인증 확인
        if (!user.getEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다. 이메일을 확인해주세요.");
        }

        String token = tokenProvider.createToken(user.getId(), user.getUserId());

        return LoginResponse.builder()
                .token(token)
                .user(UserResponse.from(user))
                .build();
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return UserResponse.from(user);
    }

    public boolean isUserIdAvailable(String userId) {
        return !userRepository.existsByUserId(userId);
    }

    public String findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정을 찾을 수 없습니다"));
        return user.getUserId();
    }

    @Transactional
    public void sendPasswordResetOtp(String userId, String email) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디와 이메일이 일치하지 않습니다");
        }

        emailService.sendOtpCode(email);
    }

    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        // OTP 검증
        boolean verified = emailService.verifyOtpCode(email, otpCode);
        if (!verified) {
            throw new IllegalArgumentException("인증 코드가 유효하지 않습니다");
        }

        // 비밀번호 재설정
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
