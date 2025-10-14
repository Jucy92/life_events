package com.example.giftmoney.controller;

import com.example.giftmoney.dto.GiftMoneyRequest;
import com.example.giftmoney.dto.GiftMoneyResponse;
import com.example.giftmoney.dto.GiftMoneyStatisticsResponse;
import com.example.giftmoney.service.GiftMoneyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gift-money")
@RequiredArgsConstructor
public class GiftMoneyController {

    private final GiftMoneyService service;

    @PostMapping
    public ResponseEntity<GiftMoneyResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GiftMoneyRequest request) {
        GiftMoneyResponse response = service.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<GiftMoneyResponse>> findAll(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String transactionType) {
        // 날짜 기준 내림차순 정렬 (최신순)
        PageRequest pageRequest = PageRequest.of(page, size,
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "eventDate"));
        Page<GiftMoneyResponse> response = service.findAll(userId, pageRequest, search, transactionType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GiftMoneyResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        GiftMoneyResponse response = service.findById(userId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GiftMoneyResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody GiftMoneyRequest request) {
        GiftMoneyResponse response = service.update(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<GiftMoneyStatisticsResponse> getStatistics(
            @AuthenticationPrincipal Long userId) {
        GiftMoneyStatisticsResponse stats = service.getStatistics(userId);
        return ResponseEntity.ok(stats);
    }

}
