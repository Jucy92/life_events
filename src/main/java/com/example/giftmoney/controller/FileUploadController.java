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

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Excel 파일만 업로드 가능합니다 (.xlsx, .xls)");
        }

        FileUploadResponse response = fileUploadService.uploadExcel(userId, file);
        return ResponseEntity.ok(response);
    }

}
