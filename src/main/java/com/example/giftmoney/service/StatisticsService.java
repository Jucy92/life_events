package com.example.giftmoney.service;

import com.example.giftmoney.dto.*;
import com.example.giftmoney.repository.GiftMoneyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final GiftMoneyRepository repository;

    /**
     * 연도별 통계 조회
     */
    public List<YearlyStatisticsDto> getYearlyStatistics(Long userId) {
        return repository.getYearlyStatistics(userId);
    }

    /**
     * 인물별 통계 조회
     */
    public List<PersonStatisticsDto> getPersonStatistics(Long userId) {
        List<Object[]> results = repository.getPersonStatisticsRaw(userId);
        List<PersonStatisticsDto> statistics = new ArrayList<>();

        for (Object[] row : results) {
            PersonStatisticsDto dto = PersonStatisticsDto.builder()
                    .giverName((String) row[0])
                    .relation((String) row[1])
                    .receivedTotal((BigDecimal) row[2])
                    .receivedCount(((Number) row[3]).longValue())
                    .sentTotal((BigDecimal) row[4])
                    .sentCount(((Number) row[5]).longValue())
                    .balance((BigDecimal) row[6])
                    .lastEventDate(row[7] != null ? ((Date) row[7]).toLocalDate() : null)
                    .lastEventType((String) row[8])
                    .build();
            statistics.add(dto);
        }

        return statistics;
    }

    /**
     * 행사 유형별 통계 조회
     */
    public List<EventTypeStatisticsDto> getEventTypeStatistics(Long userId) {
        List<Object[]> results = repository.getEventTypeStatisticsRaw(userId);
        List<EventTypeStatisticsDto> statistics = new ArrayList<>();

        for (Object[] row : results) {
            EventTypeStatisticsDto dto = EventTypeStatisticsDto.builder()
                    .eventType((String) row[0])
                    .receivedTotal((BigDecimal) row[1])
                    .receivedCount(((Number) row[2]).longValue())
                    .sentTotal((BigDecimal) row[3])
                    .sentCount(((Number) row[4]).longValue())
                    .averageReceived((BigDecimal) row[5])
                    .averageSent((BigDecimal) row[6])
                    .build();
            statistics.add(dto);
        }

        return statistics;
    }

    /**
     * 월별 통계 조회 (최근 N개월)
     */
    public List<MonthlyStatisticsDto> getMonthlyStatistics(Long userId, Integer months) {
        if (months == null || months <= 0) {
            months = 12; // 기본값: 12개월
        }

        LocalDate startDate = LocalDate.now().minusMonths(months);
        return repository.getMonthlyStatistics(userId, startDate);
    }

    /**
     * 관계별 통계 조회
     */
    public List<RelationStatisticsDto> getRelationStatistics(Long userId) {
        List<Object[]> results = repository.getRelationStatisticsRaw(userId);
        List<RelationStatisticsDto> statistics = new ArrayList<>();

        for (Object[] row : results) {
            RelationStatisticsDto dto = RelationStatisticsDto.builder()
                    .relation((String) row[0])
                    .receivedTotal((BigDecimal) row[1])
                    .receivedCount(((Number) row[2]).longValue())
                    .sentTotal((BigDecimal) row[3])
                    .sentCount(((Number) row[4]).longValue())
                    .averageReceived((BigDecimal) row[5])
                    .averageSent((BigDecimal) row[6])
                    .build();
            statistics.add(dto);
        }

        return statistics;
    }

}
