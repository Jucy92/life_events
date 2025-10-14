package com.example.giftmoney.repository;

import com.example.giftmoney.domain.entity.GiftMoney;
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

    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId AND g.giverName LIKE %:name%")
    Page<GiftMoney> findByUserIdAndGiverNameContaining(@Param("userId") Long userId, @Param("name") String name, Pageable pageable);

    // transactionType 필터링 추가 (JOIN FETCH 포함)
    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId AND g.transactionType = :transactionType")
    Page<GiftMoney> findByUserIdAndTransactionType(@Param("userId") Long userId, @Param("transactionType") String transactionType, Pageable pageable);

    @Query("SELECT g FROM GiftMoney g JOIN FETCH g.user WHERE g.user.id = :userId AND g.transactionType = :transactionType AND g.giverName LIKE %:name%")
    Page<GiftMoney> findByUserIdAndTransactionTypeAndGiverNameContaining(
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
    Object[] getStatisticsRaw(@Param("userId") Long userId);

}
