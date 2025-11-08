package com.example.giftmoney.repository;

import com.example.giftmoney.domain.entity.GiftMoney;
import com.example.giftmoney.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GiftMoneyRepository extends JpaRepository<GiftMoney, Long> {

    // ⚡ 성능: JOIN FETCH로 N+1 쿼리 문제 해결
    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId")
    Page<GiftMoney> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId AND g.name LIKE %:name%")
    Page<GiftMoney> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name, Pageable pageable);

    // transactionType 필터링 추가 (JOIN FETCH 포함)
    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId AND g.transactionType = :transactionType")
    Page<GiftMoney> findByUserIdAndTransactionType(@Param("userId") Long userId, @Param("transactionType") String transactionType, Pageable pageable);

    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId AND g.transactionType = :transactionType AND g.name LIKE %:name%")
    Page<GiftMoney> findByUserIdAndTransactionTypeAndNameContaining(
        @Param("userId") Long userId, @Param("transactionType") String transactionType, @Param("name") String name, Pageable pageable);

    Optional<GiftMoney> findByIdAndUserId(Long id, Long userId);

    List<GiftMoney> findByUserIdAndEventDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT SUM(g.amount) FROM GiftMoney g WHERE g.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(g) FROM GiftMoney g WHERE g.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(g.amount) FROM GiftMoney g WHERE g.user.id = :userId")
    Double getAverageAmountByUserId(@Param("userId") Long userId);

    // transactionType별 통계 쿼리 추가
    @Query("SELECT SUM(g.amount) FROM GiftMoney g WHERE g.user.id = :userId AND g.transactionType = :transactionType")
    BigDecimal getTotalAmountByUserIdAndTransactionType(@Param("userId") Long userId, @Param("transactionType") String transactionType);

    @Query("SELECT COUNT(g) FROM GiftMoney g WHERE g.user.id = :userId AND g.transactionType = :transactionType")
    long countByUserIdAndTransactionType(@Param("userId") Long userId, @Param("transactionType") String transactionType);

    // ⚡ 성능: 단일 쿼리로 모든 통계 조회 (6개 쿼리 → 1개 쿼리)
    @Query(value = "SELECT " +
           "COALESCE(SUM(CASE WHEN transaction_type = 'RECEIVED' THEN amount ELSE 0 END), 0) as receivedTotal, " +
           "COALESCE(COUNT(CASE WHEN transaction_type = 'RECEIVED' THEN 1 END), 0) as receivedCount, " +
           "COALESCE(AVG(CASE WHEN transaction_type = 'RECEIVED' THEN amount END), 0.0) as receivedAvg, " +
           "COALESCE(SUM(CASE WHEN transaction_type = 'SENT' THEN amount ELSE 0 END), 0) as sentTotal, " +
           "COALESCE(COUNT(CASE WHEN transaction_type = 'SENT' THEN 1 END), 0) as sentCount, " +
           "COALESCE(AVG(CASE WHEN transaction_type = 'SENT' THEN amount END), 0.0) as sentAvg, " +
           "COALESCE(SUM(amount), 0) as total, " +
           "COALESCE(COUNT(*), 0) as totalCount, " +
           "COALESCE(AVG(amount), 0.0) as avgAmount " +
           "FROM gift_money WHERE user_id = :userId",
           nativeQuery = true)
    List<Object[]> getStatisticsRaw(@Param("userId") Long userId);

    // ========== 통계 전용 쿼리 메서드 ==========

    // 연도별 통계
    @Query("SELECT new com.example.giftmoney.dto.YearlyStatisticsDto(" +
           "YEAR(g.eventDate), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'RECEIVED' THEN g.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'RECEIVED' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'SENT' THEN g.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'SENT' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'RECEIVED' THEN g.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'SENT' THEN g.amount ELSE 0 END), 0)) " +
           "FROM GiftMoney g " +
           "WHERE g.user.id = :userId " +
           "GROUP BY YEAR(g.eventDate) " +
           "ORDER BY YEAR(g.eventDate) DESC")
    List<YearlyStatisticsDto> getYearlyStatistics(@Param("userId") Long userId);

    // 인물별 통계 (Native Query - 서브쿼리 필요)
    @Query(value = "SELECT " +
           "g.name as name, " +
           "g.relation as relation, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) as receivedTotal, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END), 0) as receivedCount, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0) as sentTotal, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END), 0) as sentCount, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0) as balance, " +
           "MAX(g.event_date) as lastEventDate, " +
           "(SELECT g2.event_type FROM gift_money g2 " +
           " WHERE g2.name = g.name AND g2.user_id = :userId " +
           " ORDER BY g2.event_date DESC LIMIT 1) as lastEventType " +
           "FROM gift_money g " +
           "WHERE g.user_id = :userId " +
           "GROUP BY g.name, g.relation " +
           "ORDER BY (COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) - " +
           "         COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0)) DESC",
           nativeQuery = true)
    List<Object[]> getPersonStatisticsRaw(@Param("userId") Long userId);

    // 행사 유형별 통계
    @Query(value = "SELECT " +
           "g.event_type as eventType, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) as receivedTotal, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END), 0) as receivedCount, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0) as sentTotal, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END), 0) as sentCount, " +
           "CASE WHEN SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END) > 0 " +
           " THEN COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) / " +
           "      SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END) ELSE 0 END as averageReceived, " +
           "CASE WHEN SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END) > 0 " +
           " THEN COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0) / " +
           "      SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END) ELSE 0 END as averageSent " +
           "FROM gift_money g " +
           "WHERE g.user_id = :userId " +
           "GROUP BY g.event_type " +
           "ORDER BY (COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) + " +
           "         COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0)) DESC",
           nativeQuery = true)
    List<Object[]> getEventTypeStatisticsRaw(@Param("userId") Long userId);

    // 월별 통계 (최근 N개월)
    @Query("SELECT new com.example.giftmoney.dto.MonthlyStatisticsDto(" +
           "YEAR(g.eventDate), " +
           "MONTH(g.eventDate), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'RECEIVED' THEN g.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'RECEIVED' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'SENT' THEN g.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN g.transactionType = 'SENT' THEN 1L ELSE 0L END), 0L)) " +
           "FROM GiftMoney g " +
           "WHERE g.user.id = :userId " +
           "AND g.eventDate >= :startDate " +
           "GROUP BY YEAR(g.eventDate), MONTH(g.eventDate) " +
           "ORDER BY YEAR(g.eventDate) DESC, MONTH(g.eventDate) DESC")
    List<MonthlyStatisticsDto> getMonthlyStatistics(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    // 관계별 통계
    @Query(value = "SELECT " +
           "COALESCE(g.relation, '미지정') as relation, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) as receivedTotal, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END), 0) as receivedCount, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0) as sentTotal, " +
           "COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END), 0) as sentCount, " +
           "CASE WHEN SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END) > 0 " +
           " THEN COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) / " +
           "      SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN 1 ELSE 0 END) ELSE 0 END as averageReceived, " +
           "CASE WHEN SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END) > 0 " +
           " THEN COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0) / " +
           "      SUM(CASE WHEN g.transaction_type = 'SENT' THEN 1 ELSE 0 END) ELSE 0 END as averageSent " +
           "FROM gift_money g " +
           "WHERE g.user_id = :userId " +
           "GROUP BY COALESCE(g.relation, '미지정') " +
           "ORDER BY (COALESCE(SUM(CASE WHEN g.transaction_type = 'RECEIVED' THEN g.amount ELSE 0 END), 0) + " +
           "         COALESCE(SUM(CASE WHEN g.transaction_type = 'SENT' THEN g.amount ELSE 0 END), 0)) DESC",
           nativeQuery = true)
    List<Object[]> getRelationStatisticsRaw(@Param("userId") Long userId);

}
