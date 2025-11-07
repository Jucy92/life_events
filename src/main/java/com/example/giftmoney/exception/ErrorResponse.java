package com.example.giftmoney.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private Map<String, String> errors;

    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.errors = null;
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.errors = errors;
    }

}
