package com.smartspend.service;

import com.smartspend.domain.Expense;
import com.smartspend.domain.Member;
import com.smartspend.domain.ReportFile;
import com.smartspend.domain.ReportJob;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.dto.ReportJobDTO;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import com.smartspend.repository.ReportFileRepository;
import com.smartspend.repository.ReportJobRepository;
import com.smartspend.util.ExcelReportGenerator;
import com.smartspend.util.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportJobRepository reportJobRepository;
    private final ReportFileRepository reportFileRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private static final String REPORT_UPLOAD_PATH = "./uploads/reports";
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostConstruct
    public void init() {
        File reportFolder = new File(REPORT_UPLOAD_PATH);
        if (!reportFolder.exists()) {
            reportFolder.mkdirs();
            log.info("📁 리포트 업로드 디렉토리 생성: {}", reportFolder.getAbsolutePath());
        }
    }

    @Override
    public Long generateReport(ReportJobDTO reportJobDTO, Long userId) {
        log.info("🔍 리포트 생성 요청 - userId: {}, reportType: {}", userId, reportJobDTO.getReportType());

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // ReportJob 엔티티 생성
        ReportJob reportJob = ReportJob.builder()
                .requestedBy(member)
                .scopeType(reportJobDTO.getScopeType())
                .scopeValue(reportJobDTO.getScopeValue())
                .periodStart(reportJobDTO.getPeriodStart())
                .periodEnd(reportJobDTO.getPeriodEnd())
                .template(reportJobDTO.getTemplate())
                .reportType(reportJobDTO.getReportType())
                .status("QUEUED")
                .build();

        ReportJob saved = reportJobRepository.save(reportJob);

        // 비동기로 리포트 생성 처리
        processReportAsync(saved.getId());

        log.info("✅ 리포트 생성 작업 등록 완료 - reportJobId: {}", saved.getId());
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ReportJobDTO> getReportList(Long userId, PageRequestDTO pageRequestDTO) {
        if (pageRequestDTO == null) {
            pageRequestDTO = PageRequestDTO.builder().page(1).size(15).build();
        }

        Pageable pageable = pageRequestDTO.getPageable("createdAt");
        Page<ReportJob> result = reportJobRepository.findByRequestedBy(userId, pageable);

        List<ReportJobDTO> dtoList = result.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<ReportJobDTO>withAll()
                .dtoList(dtoList)
                .totalCount(result.getTotalElements())
                .pageRequestDTO(pageRequestDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportJobDTO getReport(Long reportId, Long userId) {
        ReportJob reportJob = reportJobRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("리포트를 찾을 수 없습니다."));

        if (!reportJob.getRequestedBy().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return entityToDTO(reportJob);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadReport(Long reportId, Long userId) {
        ReportJob reportJob = reportJobRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("리포트를 찾을 수 없습니다."));

        if (!reportJob.getRequestedBy().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        Optional<ReportFile> reportFileOpt = reportFileRepository.findFirstByReportJobIdOrderByCreatedAtDesc(reportId);
        if (reportFileOpt.isEmpty()) {
            throw new RuntimeException("리포트 파일을 찾을 수 없습니다.");
        }

        ReportFile reportFile = reportFileOpt.get();
        try {
            Path filePath = Paths.get(REPORT_UPLOAD_PATH, reportFile.getFileName());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("❌ 리포트 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("리포트 파일을 읽을 수 없습니다.");
        }
    }

    private ReportJobDTO entityToDTO(ReportJob reportJob) {
        Optional<ReportFile> reportFileOpt = reportFileRepository.findFirstByReportJobIdOrderByCreatedAtDesc(reportJob.getId());

        ReportJobDTO.ReportJobDTOBuilder builder = ReportJobDTO.builder()
                .id(reportJob.getId())
                .requestedBy(reportJob.getRequestedBy().getId())
                .requestedByName(reportJob.getRequestedBy().getName())
                .scopeType(reportJob.getScopeType())
                .scopeValue(reportJob.getScopeValue())
                .periodStart(reportJob.getPeriodStart())
                .periodEnd(reportJob.getPeriodEnd())
                .template(reportJob.getTemplate())
                .reportType(reportJob.getReportType())
                .status(reportJob.getStatus())
                .createdAt(reportJob.getCreatedAt())
                .updatedAt(reportJob.getUpdatedAt());

        if (reportFileOpt.isPresent()) {
            ReportFile reportFile = reportFileOpt.get();
            builder.fileName(reportFile.getFileName())
                    .fileUrl(reportFile.getFileUrl())
                    .fileType(reportFile.getFileType());
        }

        return builder.build();
    }

    // 비동기 리포트 생성 처리
    @Async
    @Transactional
    public void processReportAsync(Long reportJobId) {
        try {
            ReportJob reportJob = reportJobRepository.findById(reportJobId)
                    .orElseThrow(() -> new RuntimeException("리포트 작업을 찾을 수 없습니다."));

            reportJob.markAsRunning();
            reportJobRepository.save(reportJob);

            log.info("🔄 리포트 생성 시작 - reportJobId: {}, reportType: {}", reportJobId, reportJob.getReportType());

            // 지출 데이터 조회
            List<Expense> expenses = fetchExpensesForReport(reportJob);
            
            if (expenses.isEmpty()) {
                log.warn("⚠️ 리포트 생성할 데이터가 없습니다 - reportJobId: {}", reportJobId);
                reportJob.markAsFailed();
                reportJobRepository.save(reportJob);
                return;
            }

            // 리포트 파일 생성
            byte[] fileData;
            String fileExtension;
            String period = reportJob.getPeriodStart().format(PERIOD_FORMATTER) + " ~ " + 
                           reportJob.getPeriodEnd().format(PERIOD_FORMATTER);

            if ("EXCEL".equals(reportJob.getTemplate())) {
                if ("DEPT_EXCEL".equals(reportJob.getReportType())) {
                    fileData = ExcelReportGenerator.generateDeptExcel(
                        expenses, 
                        reportJob.getScopeValue(), 
                        "부서별 지출 보고서 - " + period
                    );
                } else {
                    fileData = ExcelReportGenerator.generateDetailExcel(
                        expenses, 
                        "상세 지출 보고서 - " + period
                    );
                }
                fileExtension = "xlsx";
            } else {
                // PDF
                if ("PERSONAL_PDF".equals(reportJob.getReportType())) {
                    fileData = PdfReportGenerator.generatePersonalPdf(
                        expenses,
                        reportJob.getRequestedBy().getName(),
                        period
                    );
                } else {
                    fileData = PdfReportGenerator.generateDeptPdf(
                        expenses,
                        reportJob.getScopeValue(),
                        period
                    );
                }
                fileExtension = "pdf";
            }

            // 파일 저장
            String fileName = String.format("report_%d_%s.%s", 
                reportJobId, 
                System.currentTimeMillis(), 
                fileExtension);
            Path filePath = Paths.get(REPORT_UPLOAD_PATH, fileName);
            
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(fileData);
            }

            // 체크섬 계산
            String checksum = calculateChecksum(fileData);

            // 리포트 파일 저장
            ReportFile reportFile = ReportFile.builder()
                    .reportJob(reportJob)
                    .fileName(fileName)
                    .fileUrl("/api/admin/accounting/reports/" + reportJobId + "/download")
                    .fileType(reportJob.getTemplate().equals("EXCEL") ? "XLSX" : "PDF")
                    .fileSize((long) fileData.length)
                    .checksum(checksum)
                    .build();

            reportFileRepository.save(reportFile);

            reportJob.markAsDone();
            reportJobRepository.save(reportJob);

            log.info("✅ 리포트 생성 완료 - reportJobId: {}, 파일명: {}, 크기: {} bytes", 
                    reportJobId, fileName, fileData.length);
        } catch (Exception e) {
            log.error("❌ 리포트 생성 실패 - reportJobId: {}, 에러: {}", reportJobId, e.getMessage(), e);
            try {
                ReportJob reportJob = reportJobRepository.findById(reportJobId).orElse(null);
                if (reportJob != null) {
                    reportJob.markAsFailed();
                    reportJobRepository.save(reportJob);
                    log.info("📝 리포트 작업 상태를 FAILED로 변경 - reportJobId: {}", reportJobId);
                } else {
                    log.warn("⚠️ 리포트 작업을 찾을 수 없음 - reportJobId: {}", reportJobId);
                }
            } catch (Exception saveException) {
                log.error("❌ 리포트 작업 상태 저장 실패 - reportJobId: {}", reportJobId, saveException);
            }
        }
    }

    /**
     * 리포트용 지출 데이터 조회
     */
    private List<Expense> fetchExpensesForReport(ReportJob reportJob) {
        String scopeType = reportJob.getScopeType();
        String scopeValue = reportJob.getScopeValue();
        LocalDate startDate = reportJob.getPeriodStart();
        LocalDate endDate = reportJob.getPeriodEnd();

        if ("MY".equals(scopeType)) {
            // 개인 지출
            return expenseRepository.findForReportByUserIdAndDateRange(
                reportJob.getRequestedBy().getId(),
                startDate,
                endDate
            );
        } else if ("DEPARTMENT_NAME".equals(scopeType) && scopeValue != null) {
            // 부서별 지출
            return expenseRepository.findForReportByDepartmentAndDateRange(
                scopeValue,
                startDate,
                endDate
            );
        } else {
            // 전체 지출
            return expenseRepository.findForReportByDateRange(startDate, endDate);
        }
    }

    /**
     * 파일 체크섬 계산 (SHA-256)
     */
    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("⚠️ 체크섬 계산 실패: {}", e.getMessage());
            return null;
        }
    }
}

