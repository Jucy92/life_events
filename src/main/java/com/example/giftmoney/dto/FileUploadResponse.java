package com.example.giftmoney.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileUploadResponse {

    private int successCount;
    private int failCount;
    private List<ErrorDetail> errors;

    @Getter
    @Builder
    public static class ErrorDetail {
        private int row;
        private String reason;
    }

}
