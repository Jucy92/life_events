package com.example.giftmoney.repository;

import com.example.giftmoney.domain.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByToken(String token);

    Optional<EmailVerification> findByEmailAndVerifiedFalse(String email);

    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

}
