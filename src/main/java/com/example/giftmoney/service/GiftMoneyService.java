package com.example.giftmoney.service;

import com.example.giftmoney.domain.entity.GiftMoney;
import com.example.giftmoney.domain.entity.User;
import com.example.giftmoney.dto.GiftMoneyRequest;
import com.example.giftmoney.dto.GiftMoneyResponse;
import com.example.giftmoney.dto.GiftMoneyStatisticsResponse;
import com.example.giftmoney.repository.GiftMoneyRepository;
import com.example.giftmoney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftMoneyService {

    private final GiftMoneyRepository giftMoneyRepository;
    private final UserRepository userRepository;

    @Transactional
    public GiftMoneyResponse create(Long userId, GiftMoneyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        GiftMoney entity = new GiftMoney();
        entity.setUser(user);
        entity.setEventDate(request.getEventDate());
        entity.setEventType(request.getEventType());
        entity.setTransactionType(request.getTransactionType());
        entity.setGiverName(request.getGiverName());
        entity.setGiverRelation(request.getGiverRelation());
        entity.setAmount(request.getAmount());
        entity.setContact(request.getContact());
        entity.setMemo(request.getMemo());

        GiftMoney saved = giftMoneyRepository.save(entity);
        return GiftMoneyResponse.from(saved);
    }

    public Page<GiftMoneyResponse> findAll(Long userId, Pageable pageable, String search, String transactionType) {
        Page<GiftMoney> page;

        // transactionType과 search 조건에 따라 쿼리 선택
        if (transactionType != null && !transactionType.isBlank()) {
            if (search != null && !search.isBlank()) {
                page = giftMoneyRepository.findByUserIdAndTransactionTypeAndGiverNameContaining(
                    userId, transactionType, search, pageable);
            } else {
                page = giftMoneyRepository.findByUserIdAndTransactionType(userId, transactionType, pageable);
            }
        } else {
            if (search != null && !search.isBlank()) {
                page = giftMoneyRepository.findByUserIdAndGiverNameContaining(userId, search, pageable);
            } else {
                page = giftMoneyRepository.findByUserId(userId, pageable);
            }
        }

        return page.map(GiftMoneyResponse::from);
    }

    public GiftMoneyResponse findById(Long userId, Long id) {
        GiftMoney entity = giftMoneyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다"));
        return GiftMoneyResponse.from(entity);
    }

    @Transactional
    public GiftMoneyResponse update(Long userId, Long id, GiftMoneyRequest request) {
        GiftMoney entity = giftMoneyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다"));

        entity.setEventDate(request.getEventDate());
        entity.setEventType(request.getEventType());
        entity.setTransactionType(request.getTransactionType());
        entity.setGiverName(request.getGiverName());
        entity.setGiverRelation(request.getGiverRelation());
        entity.setAmount(request.getAmount());
        entity.setContact(request.getContact());
        entity.setMemo(request.getMemo());

        GiftMoney updated = giftMoneyRepository.save(entity);
        return GiftMoneyResponse.from(updated);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        GiftMoney entity = giftMoneyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다"));
        giftMoneyRepository.delete(entity);
    }

    // 받은/보낸 경조금 통계 조회
    // ⚡ 성능: 단일 쿼리로 최적화 (기존 6개 쿼리 → 1개 쿼리)
    public GiftMoneyStatisticsResponse getStatistics(Long userId) {
        List<Object[]> results = giftMoneyRepository.getStatisticsRaw(userId);

        if (results == null || results.isEmpty()) {
            return new GiftMoneyStatisticsResponse();
        }

        // Native query는 List<Object[]>로 반환됨 - 첫 번째 행을 가져옴
        Object[] result = results.get(0);

        GiftMoneyStatisticsResponse stats = new GiftMoneyStatisticsResponse();

        // 쿼리 결과 매핑 (순서: receivedTotal, receivedCount, receivedAvg, sentTotal, sentCount, sentAvg, total, totalCount, avgAmount)
        // Number 타입으로 받아서 안전하게 변환
        stats.setReceivedTotalAmount(convertToBigDecimal(result[0]));
        stats.setReceivedCount(convertToLong(result[1]));
        stats.setReceivedAvgAmount(convertToDouble(result[2]));

        stats.setSentTotalAmount(convertToBigDecimal(result[3]));
        stats.setSentCount(convertToLong(result[4]));
        stats.setSentAvgAmount(convertToDouble(result[5]));

        stats.setTotalAmount(convertToBigDecimal(result[6]));
        stats.setTotalCount(convertToLong(result[7]));
        stats.setAvgAmount(convertToDouble(result[8]));

        return stats;
    }

    // 안전한 타입 변환 헬퍼 메서드
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return BigDecimal.ZERO;
    }

    private Long convertToLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    private Double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

}
