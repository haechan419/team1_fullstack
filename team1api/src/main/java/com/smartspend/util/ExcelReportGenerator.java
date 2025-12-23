package com.smartspend.util;

import com.smartspend.domain.Expense;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
public class ExcelReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 상세 Excel 보고서 생성 (DETAIL_EXCEL)
     */
    public static byte[] generateDetailExcel(List<Expense> expenses, String title) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("지출 내역");

        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);

        // 헤더 행 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"번호", "지출일자", "가맹점명", "금액", "카테고리", "상태", "비고"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 생성
        int rowNum = 1;
        int totalAmount = 0;
        for (Expense expense : expenses) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(rowNum - 1); // 번호
            row.createCell(1).setCellValue(expense.getReceiptDate().format(DATE_FORMATTER)); // 지출일자
            row.createCell(2).setCellValue(expense.getMerchant() != null ? expense.getMerchant() : ""); // 가맹점명
            Cell amountCell = row.createCell(3);
            amountCell.setCellValue(expense.getAmount());
            amountCell.setCellStyle(numberStyle); // 금액
            row.createCell(4).setCellValue(expense.getCategory() != null ? expense.getCategory() : ""); // 카테고리
            row.createCell(5).setCellValue(expense.getStatus().name()); // 상태
            row.createCell(6).setCellValue(expense.getDescription() != null ? expense.getDescription() : ""); // 비고

            totalAmount += expense.getAmount();
        }

        // 합계 행 추가
        Row totalRow = sheet.createRow(rowNum);
        Cell totalLabelCell = totalRow.createCell(2);
        totalLabelCell.setCellValue("합계");
        totalLabelCell.setCellStyle(headerStyle);
        Cell totalAmountCell = totalRow.createCell(3);
        totalAmountCell.setCellValue(totalAmount);
        totalAmountCell.setCellStyle(numberStyle);

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1000, 15000));
        }

        // 파일로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        log.info("✅ Excel 보고서 생성 완료 - 총 {}건, 합계: {}원", expenses.size(), totalAmount);
        return outputStream.toByteArray();
    }

    /**
     * 부서별 Excel 보고서 생성 (DEPT_EXCEL)
     */
    public static byte[] generateDeptExcel(List<Expense> expenses, String departmentName, String title) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(departmentName != null ? departmentName : "전체");

        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);

        // 제목 행
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);

        // 헤더 행 생성
        Row headerRow = sheet.createRow(2);
        String[] headers = {"번호", "사원명", "지출일자", "가맹점명", "금액", "카테고리", "비고"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 생성
        int rowNum = 3;
        int totalAmount = 0;
        for (Expense expense : expenses) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(rowNum - 3); // 번호
            row.createCell(1).setCellValue(expense.getWriter().getName()); // 사원명
            row.createCell(2).setCellValue(expense.getReceiptDate().format(DATE_FORMATTER)); // 지출일자
            row.createCell(3).setCellValue(expense.getMerchant() != null ? expense.getMerchant() : ""); // 가맹점명
            Cell amountCell = row.createCell(4);
            amountCell.setCellValue(expense.getAmount());
            amountCell.setCellStyle(numberStyle); // 금액
            row.createCell(5).setCellValue(expense.getCategory() != null ? expense.getCategory() : ""); // 카테고리
            row.createCell(6).setCellValue(expense.getDescription() != null ? expense.getDescription() : ""); // 비고

            totalAmount += expense.getAmount();
        }

        // 합계 행 추가
        Row totalRow = sheet.createRow(rowNum);
        Cell totalLabelCell = totalRow.createCell(3);
        totalLabelCell.setCellValue("합계");
        totalLabelCell.setCellStyle(headerStyle);
        Cell totalAmountCell = totalRow.createCell(4);
        totalAmountCell.setCellValue(totalAmount);
        totalAmountCell.setCellStyle(numberStyle);

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1000, 15000));
        }

        // 파일로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        log.info("✅ 부서별 Excel 보고서 생성 완료 - 부서: {}, 총 {}건, 합계: {}원", 
                departmentName != null ? departmentName : "전체", expenses.size(), totalAmount);
        return outputStream.toByteArray();
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}

