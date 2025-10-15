package com.example.giftmoney.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * CSV 템플릿 다운로드 컨트롤러
 * 동적으로 CSV 파일을 생성하여 다운로드 제공
 */
@RestController
@RequestMapping("/api/template")
public class TemplateController {

    /**
     * 경조금 업로드 CSV 템플릿 다운로드
     *
     * 동작 방식:
     * 1. CSV 형식의 문자열을 메모리에서 생성
     * 2. HTTP 응답 헤더에 다운로드 정보 설정
     *    - Content-Type: text/csv (CSV 파일임을 명시)
     *    - Content-Disposition: attachment (다운로드 동작 유도)
     * 3. 브라우저가 파일로 저장
     *
     * @return CSV 문자열과 다운로드 헤더가 포함된 ResponseEntity
     */
    @GetMapping("/download")
    public ResponseEntity<String> downloadTemplate() {
        // CSV 헤더 (컬럼명)
        StringBuilder csv = new StringBuilder();

        // UTF-8 BOM 추가 (Excel에서 한글 깨짐 방지)
        // BOM: Byte Order Mark - Excel이 UTF-8 인코딩을 인식하도록 함
        csv.append("\uFEFF");

        csv.append("event_date,event_type,giver_name,giver_relation,amount,contact,memo\n");

        // 예시 데이터 (현재 날짜 기준으로 동적 생성)
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 예시 1: 결혼식 (오늘 날짜)
        csv.append(String.format("%s,결혼식,홍길동,친구,100000,010-1234-5678,대학 동기\n",
            today.format(formatter)));

        // 예시 2: 장례식 (+5일)
        csv.append(String.format("%s,장례식,김철수,가족,200000,010-9876-5432,삼촌\n",
            today.plusDays(5).format(formatter)));

        // 예시 3: 돌잔치 (+15일)
        csv.append(String.format("%s,돌잔치,이영희,직장동료,50000,010-5555-1234,같은 팀\n",
            today.plusDays(15).format(formatter)));

        // HTTP 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();

        // Content-Type: CSV 파일 형식 지정
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));

        // Content-Disposition: 다운로드 파일명 지정
        // attachment → 브라우저가 표시하지 않고 다운로드
        // filename → 저장될 파일 이름
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=gift_money_template.csv");

        // ResponseEntity로 CSV 문자열과 헤더 반환
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }

    /**
     * CSV 파일 형식 설명 엔드포인트 (선택 사항)
     * API 문서화 목적으로 CSV 형식 정보 제공
     *
     * @return CSV 형식 설명 JSON
     */
    @GetMapping("/format-info")
    public ResponseEntity<CsvFormatInfo> getCsvFormatInfo() {
        CsvFormatInfo info = new CsvFormatInfo();
        info.setColumns(new String[]{
            "event_date", "event_type", "giver_name",
            "giver_relation", "amount", "contact", "memo"
        });
        info.setDescriptions(new String[]{
            "행사 날짜 (yyyy-MM-dd 형식)",
            "행사 유형 (결혼식, 장례식, 돌잔치, 개업, 기타)",
            "보낸 사람 이름",
            "관계 (친구, 가족, 직장동료 등)",
            "금액 (숫자만, 쉼표 없이)",
            "연락처 (010-1234-5678 형식)",
            "메모 (선택 사항)"
        });
        return ResponseEntity.ok(info);
    }

    /**
     * CSV 형식 정보를 담는 내부 클래스
     */
    static class CsvFormatInfo {
        private String[] columns;
        private String[] descriptions;

        public String[] getColumns() {
            return columns;
        }

        public void setColumns(String[] columns) {
            this.columns = columns;
        }

        public String[] getDescriptions() {
            return descriptions;
        }

        public void setDescriptions(String[] descriptions) {
            this.descriptions = descriptions;
        }
    }
}
