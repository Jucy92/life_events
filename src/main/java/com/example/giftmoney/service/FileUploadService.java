package com.example.giftmoney.service;

import com.example.giftmoney.domain.entity.GiftMoney;
import com.example.giftmoney.domain.entity.User;
import com.example.giftmoney.dto.FileUploadResponse;
import com.example.giftmoney.dto.FileUploadResponse.ErrorDetail;
import com.example.giftmoney.repository.GiftMoneyRepository;
import com.example.giftmoney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final GiftMoneyRepository giftMoneyRepository;
    private final UserRepository userRepository;

    @Transactional
    public FileUploadResponse uploadExcel(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        List<GiftMoney> entities = new ArrayList<>();
        List<ErrorDetail> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // 첫 번째 행은 헤더이므로 스킵
            // 먼저 모든 행을 검증하고 파싱
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    GiftMoney entity = parseRow(user, row);
                    entities.add(entity);
                } catch (Exception e) {
                    log.error("Failed to parse row {}: {}", i + 1, e.getMessage());
                    errors.add(ErrorDetail.builder()
                            .row(i + 1)
                            .reason(e.getMessage())
                            .build());
                }
            }

            // 실패가 하나라도 있으면 전체 롤백 (트랜잭션 예외 발생)
            if (!errors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Excel 업로드 실패\n\n");
                errorMessage.append("총 ").append(errors.size()).append("건의 오류가 발생했습니다.\n\n");
                errorMessage.append("오류 내역:\n");

                for (ErrorDetail error : errors) {
                    errorMessage.append("- ").append(error.getRow()).append("행: ")
                               .append(error.getReason()).append("\n");
                }

                errorMessage.append("\n양식에 맞춰 수정 후 다시 업로드해주세요.\n");
                errorMessage.append("템플릿 다운로드: 대시보드 > Excel 업로드 > 템플릿 다운로드");

                throw new IllegalArgumentException(errorMessage.toString());
            }

            // 모든 데이터가 유효한 경우에만 저장
            giftMoneyRepository.saveAll(entities);

            return FileUploadResponse.builder()
                    .successCount(entities.size())
                    .failCount(0)
                    .errors(new ArrayList<>())
                    .build();

        } catch (IOException e) {
            log.error("Failed to parse Excel file", e);
            throw new RuntimeException("Excel 파일 파싱에 실패했습니다: " + e.getMessage());
        }
    }

    private GiftMoney parseRow(User user, Row row) {
        GiftMoney entity = new GiftMoney();
        entity.setUser(user);

        // 0: event_date (날짜)
        Cell dateCell = row.getCell(0);
        if (dateCell == null) {
            throw new IllegalArgumentException("행사 날짜는 필수입니다");
        }
        LocalDate eventDate = convertToLocalDate(dateCell);
        entity.setEventDate(eventDate);

        // 1: event_type (문자열)
        Cell eventTypeCell = row.getCell(1);
        if (eventTypeCell == null) {
            throw new IllegalArgumentException("행사 유형은 필수입니다");
        }
        entity.setEventType(getCellValueAsString(eventTypeCell));

        // 2: giver_name (문자열)
        Cell giverNameCell = row.getCell(2);
        if (giverNameCell == null) {
            throw new IllegalArgumentException("보낸 사람 이름은 필수입니다");
        }
        entity.setGiverName(getCellValueAsString(giverNameCell));

        // 3: giver_relation (문자열, optional)
        Cell relationCell = row.getCell(3);
        if (relationCell != null) {
            entity.setGiverRelation(getCellValueAsString(relationCell));
        }

        // 4: amount (숫자)
        Cell amountCell = row.getCell(4);
        if (amountCell == null) {
            throw new IllegalArgumentException("금액은 필수입니다");
        }
        BigDecimal amount = convertToAmount(amountCell);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 양수여야 합니다");
        }
        entity.setAmount(amount);

        // 5: contact (문자열, optional)
        Cell contactCell = row.getCell(5);
        if (contactCell != null) {
            entity.setContact(getCellValueAsString(contactCell));
        }

        // 6: memo (문자열, optional)
        Cell memoCell = row.getCell(6);
        if (memoCell != null) {
            entity.setMemo(getCellValueAsString(memoCell));
        }

        return entity;
    }

    private LocalDate convertToLocalDate(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue();
            try {
                return LocalDate.parse(dateStr);  // ISO 형식: yyyy-MM-dd
            } catch (Exception e) {
                throw new IllegalArgumentException("올바른 날짜 형식이 아닙니다 (yyyy-MM-dd)");
            }
        } else {
            throw new IllegalArgumentException("올바른 날짜 형식이 아닙니다");
        }
    }

    private BigDecimal convertToAmount(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            String amountStr = cell.getStringCellValue().replaceAll("[^0-9.]", "");
            try {
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("올바른 금액 형식이 아닙니다");
            }
        } else {
            throw new IllegalArgumentException("올바른 금액 형식이 아닙니다");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

}
