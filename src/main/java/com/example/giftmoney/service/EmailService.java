package com.example.giftmoney.service;

import com.example.giftmoney.domain.entity.EmailVerification;
import com.example.giftmoney.domain.entity.User;
import com.example.giftmoney.repository.EmailVerificationRepository;
import com.example.giftmoney.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public void sendVerificationEmail(String email) {
        try {
            // 기존 미인증 토큰이 있으면 삭제
            verificationRepository.findByEmailAndVerifiedFalse(email)
                    .ifPresent(verificationRepository::delete);

            // 새 인증 토큰 생성
            EmailVerification verification = EmailVerification.create(email);
            verificationRepository.save(verification);

            // 이메일 발송
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("경조사비 관리 서비스 - 이메일 인증");

            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verification.getToken();
            String htmlContent = buildVerificationEmail(verificationUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("인증 이메일 발송 완료: {}", email);

        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildVerificationEmail(String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Malgun Gothic', sans-serif;
                            line-height: 1.6;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f9f9f9;
                        }
                        .content {
                            background-color: white;
                            padding: 30px;
                            border-radius: 5px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            margin: 20px 0;
                            background-color: #007bff;
                            color: white !important;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                        }
                        .footer {
                            margin-top: 20px;
                            padding-top: 20px;
                            border-top: 1px solid #ddd;
                            font-size: 12px;
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="content">
                            <h2>이메일 인증</h2>
                            <p>경조사비 관리 서비스에 가입해 주셔서 감사합니다.</p>
                            <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">이메일 인증하기</a>
                            </div>
                            <p>또는 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>
                            <p style="word-break: break-all; color: #007bff;">%s</p>
                            <div class="footer">
                                <p>본 링크는 24시간 동안 유효합니다.</p>
                                <p>본인이 요청하지 않은 경우 이 이메일을 무시하셔도 됩니다.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationUrl, verificationUrl);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerification verification = verificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (verification.getVerified()) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        }

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증 토큰이 만료되었습니다. 다시 시도해주세요.");
        }

        verification.setVerified(true);
        verificationRepository.save(verification);

        // User 엔티티의 emailVerified도 업데이트
        User user = userRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("이메일 인증 완료: {}", verification.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return verificationRepository.findByEmailAndVerifiedFalse(email).isEmpty();
    }

    @Transactional
    public void sendOtpCode(String email) {
        try {
            // 기존 미인증 OTP 삭제
            verificationRepository.findByEmailAndVerifiedFalse(email)
                    .ifPresent(verificationRepository::delete);

            // 새 OTP 생성
            EmailVerification verification = EmailVerification.createWithOtp(email);
            verificationRepository.save(verification);

            // 이메일 발송
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("경조사비 관리 서비스 - 이메일 인증 코드");

            String htmlContent = buildOtpEmail(verification.getOtpCode());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP 코드 발송 완료: {}", email);

        } catch (MessagingException e) {
            log.error("OTP 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildOtpEmail(String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Malgun Gothic', sans-serif;
                            line-height: 1.6;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f9f9f9;
                        }
                        .content {
                            background-color: white;
                            padding: 30px;
                            border-radius: 5px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .otp-code {
                            font-size: 32px;
                            font-weight: bold;
                            color: #667eea;
                            text-align: center;
                            padding: 20px;
                            margin: 20px 0;
                            background-color: #f0f0f0;
                            border-radius: 8px;
                            letter-spacing: 8px;
                        }
                        .footer {
                            margin-top: 20px;
                            padding-top: 20px;
                            border-top: 1px solid #ddd;
                            font-size: 12px;
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="content">
                            <h2>이메일 인증 코드</h2>
                            <p>경조사비 관리 서비스 회원가입을 위한 인증 코드입니다.</p>
                            <p>아래 6자리 코드를 입력해주세요:</p>
                            <div class="otp-code">%s</div>
                            <div class="footer">
                                <p>본 인증 코드는 10분 동안 유효합니다.</p>
                                <p>본인이 요청하지 않은 경우 이 이메일을 무시하셔도 됩니다.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otpCode);
    }

    @Transactional
    public boolean verifyOtpCode(String email, String otpCode) {
        // ⚠️ 보안 경고: 개발용 백도어 코드 비활성화됨 (2025-11-07)
        // 프로덕션 환경에서는 절대 활성화하지 마세요!
        /*
        // 개발용 백도어: 920505 입력 시 바로 인증 통과
        if ("920505".equals(otpCode)) {
            log.warn("개발용 백도어 OTP 사용: {}", email);

            // 기존 인증 레코드가 있으면 verified 처리, 없으면 새로 생성
            EmailVerification verification = verificationRepository
                    .findTopByEmailOrderByCreatedAtDesc(email)
                    .orElseGet(() -> {
                        EmailVerification newVerification = EmailVerification.createWithOtp(email);
                        newVerification.setOtpCode("920505");
                        return verificationRepository.save(newVerification);
                    });

            verification.setVerified(true);
            verificationRepository.save(verification);

            log.info("OTP 인증 완료 (개발용 백도어): {}", email);
            return true;
        }
        */

        // 일반 OTP 검증 로직
        EmailVerification verification = verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드를 찾을 수 없습니다."));

        if (verification.getVerified()) {
            throw new IllegalArgumentException("이미 인증 완료된 이메일입니다.");
        }

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }

        if (!verification.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        verification.setVerified(true);
        verificationRepository.save(verification);

        log.info("OTP 인증 완료: {}", email);
        return true;
    }
}
