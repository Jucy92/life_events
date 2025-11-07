package com.example.giftmoney.controller;

import com.example.giftmoney.dto.FileUploadResponse;
import com.example.giftmoney.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gift-money")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    // ⚡ 보안: 파일 업로드 제한 설정 (보안 강화 2025-11-07)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-excel" // .xls
    );

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {

        // ⚡ 보안: 파일 비어있음 검증
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // ⚡ 보안: 파일 크기 검증 (서버 측 검증 추가)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다");
        }

        // ⚡ 보안: 파일 확장자 검증
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Excel 파일만 업로드 가능합니다 (.xlsx, .xls)");
        }

        // ⚡ 보안: MIME 타입 검증 (확장자 위조 방지)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("유효하지 않은 파일 형식입니다. Excel 파일만 업로드 가능합니다");
        }

        FileUploadResponse response = fileUploadService.uploadExcel(userId, file);
        return ResponseEntity.ok(response);
    }

}
