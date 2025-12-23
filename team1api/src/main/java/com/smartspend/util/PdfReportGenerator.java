package com.smartspend.util;

import com.smartspend.domain.Expense;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
public class PdfReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 개인 PDF 보고서 생성 (PERSONAL_PDF)
     */
    public static byte[] generatePersonalPdf(List<Expense> expenses, String userName, String period) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 제목
        Paragraph title = new Paragraph("개인 지출 보고서")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // 정보
        document.add(new Paragraph("사원명: " + userName).setMarginBottom(5));
        document.add(new Paragraph("기간: " + period).setMarginBottom(20));

        // 통계
        int totalAmount = expenses.stream().mapToInt(Expense::getAmount).sum();
        document.add(new Paragraph("총 지출 건수: " + expenses.size() + "건").setMarginBottom(5));
        document.add(new Paragraph("총 지출액: " + String.format("%,d", totalAmount) + "원").setMarginBottom(20));

        // 테이블 생성
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth()
                .setMarginTop(20);

        // 헤더
        table.addHeaderCell(new Paragraph("번호").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("지출일자").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("가맹점명").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("금액").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("카테고리").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("비고").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));

        // 데이터
        int rowNum = 1;
        for (Expense expense : expenses) {
            table.addCell(new Paragraph(String.valueOf(rowNum++)));
            table.addCell(new Paragraph(expense.getReceiptDate().format(DATE_FORMATTER)));
            table.addCell(new Paragraph(expense.getMerchant() != null ? expense.getMerchant() : ""));
            table.addCell(new Paragraph(String.format("%,d", expense.getAmount())));
            table.addCell(new Paragraph(expense.getCategory() != null ? expense.getCategory() : ""));
            table.addCell(new Paragraph(expense.getDescription() != null ? expense.getDescription() : ""));
        }

        document.add(table);

        // 합계
        Paragraph total = new Paragraph("합계: " + String.format("%,d", totalAmount) + "원")
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20);
        document.add(total);

        document.close();

        log.info("✅ 개인 PDF 보고서 생성 완료 - 사원: {}, 총 {}건, 합계: {}원", userName, expenses.size(), totalAmount);
        return outputStream.toByteArray();
    }

    /**
     * 부서별 PDF 보고서 생성 (부서별 Excel과 동일한 형식)
     */
    public static byte[] generateDeptPdf(List<Expense> expenses, String departmentName, String period) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 제목
        Paragraph title = new Paragraph("부서별 지출 보고서")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // 정보
        document.add(new Paragraph("부서: " + (departmentName != null ? departmentName : "전체")).setMarginBottom(5));
        document.add(new Paragraph("기간: " + period).setMarginBottom(20));

        // 통계
        int totalAmount = expenses.stream().mapToInt(Expense::getAmount).sum();
        document.add(new Paragraph("총 지출 건수: " + expenses.size() + "건").setMarginBottom(5));
        document.add(new Paragraph("총 지출액: " + String.format("%,d", totalAmount) + "원").setMarginBottom(20));

        // 테이블 생성
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth()
                .setMarginTop(20);

        // 헤더
        table.addHeaderCell(new Paragraph("번호").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("사원명").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("지출일자").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("가맹점명").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("금액").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("카테고리").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Paragraph("비고").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));

        // 데이터
        int rowNum = 1;
        for (Expense expense : expenses) {
            table.addCell(new Paragraph(String.valueOf(rowNum++)));
            table.addCell(new Paragraph(expense.getWriter().getName()));
            table.addCell(new Paragraph(expense.getReceiptDate().format(DATE_FORMATTER)));
            table.addCell(new Paragraph(expense.getMerchant() != null ? expense.getMerchant() : ""));
            table.addCell(new Paragraph(String.format("%,d", expense.getAmount())));
            table.addCell(new Paragraph(expense.getCategory() != null ? expense.getCategory() : ""));
            table.addCell(new Paragraph(expense.getDescription() != null ? expense.getDescription() : ""));
        }

        document.add(table);

        // 합계
        Paragraph total = new Paragraph("합계: " + String.format("%,d", totalAmount) + "원")
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20);
        document.add(total);

        document.close();

        log.info("✅ 부서별 PDF 보고서 생성 완료 - 부서: {}, 총 {}건, 합계: {}원", 
                departmentName != null ? departmentName : "전체", expenses.size(), totalAmount);
        return outputStream.toByteArray();
    }
}

