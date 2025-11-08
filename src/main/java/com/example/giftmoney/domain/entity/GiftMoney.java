package com.example.giftmoney.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_money", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_event_date", columnList = "event_date"),
    @Index(name = "idx_name", columnList = "name")
})
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class GiftMoney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 10)
    private String transactionType = "RECEIVED";  // RECEIVED(받은 축의금) or SENT(보낸 축의금)

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "relation", length = 50)
    private String relation;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal amount;

    @Column(length = 50)
    private String contact;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
