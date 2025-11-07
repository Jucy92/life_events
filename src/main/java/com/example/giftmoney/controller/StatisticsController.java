package com.example.giftmoney.controller;

import com.example.giftmoney.dto.*;
import com.example.giftmoney.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 연도별 통계 조회
     */
    @GetMapping("/yearly")
    public ResponseEntity<List<YearlyStatisticsDto>> getYearlyStatistics(
            @AuthenticationPrincipal Long userId) {
        List<YearlyStatisticsDto> stats = statisticsService.getYearlyStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 인물별 통계 조회
     */
    @GetMapping("/person")
    public ResponseEntity<List<PersonStatisticsDto>> getPersonStatistics(
            @AuthenticationPrincipal Long userId) {
        List<PersonStatisticsDto> stats = statisticsService.getPersonStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 행사 유형별 통계 조회
     */
    @GetMapping("/event-type")
    public ResponseEntity<List<EventTypeStatisticsDto>> getEventTypeStatistics(
            @AuthenticationPrincipal Long userId) {
        List<EventTypeStatisticsDto> stats = statisticsService.getEventTypeStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 월별 통계 조회 (최근 N개월)
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStatisticsDto>> getMonthlyStatistics(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "12") Integer months) {
        List<MonthlyStatisticsDto> stats = statisticsService.getMonthlyStatistics(userId, months);
        return ResponseEntity.ok(stats);
    }

    /**
     * 관계별 통계 조회
     */
    @GetMapping("/relation")
    public ResponseEntity<List<RelationStatisticsDto>> getRelationStatistics(
            @AuthenticationPrincipal Long userId) {
        List<RelationStatisticsDto> stats = statisticsService.getRelationStatistics(userId);
        return ResponseEntity.ok(stats);
    }

}
