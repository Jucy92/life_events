package com.example.giftmoney.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "email_verifications")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(length = 6)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean verified = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static EmailVerification create(String email) {
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setToken(UUID.randomUUID().toString());
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setVerified(false);
        return verification;
    }

    public static EmailVerification createWithOtp(String email) {
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setToken(UUID.randomUUID().toString());
        verification.setOtpCode(generateOtp());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verification.setVerified(false);
        return verification;
    }

    private static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
