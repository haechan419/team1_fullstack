package com.demo.report.service;

import com.demo.report.constants.ReportTypes;
import com.demo.report.dto.*;
import com.demo.report.entity.ReportDownloadLog;
import com.demo.report.entity.ReportFile;
import com.demo.report.entity.ReportJob;
import com.demo.report.entity.enums.DataScope;
import com.demo.report.entity.enums.OutputFormat;
import com.demo.report.entity.enums.ReportStatus;
import com.demo.report.generator.ExcelReportGenerator;
import com.demo.report.generator.PdfReportGenerator;
import com.demo.report.repository.ReportDownloadLogRepository;
import com.demo.report.repository.ReportFileRepository;
import com.demo.report.repository.ReportJobRepository;
import com.demo.security.ReportPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportJobRepository reportJobRepository;
    private final PdfReportGenerator pdfGen;
    private final ExcelReportGenerator excelGen;
    private final ReportFileRepository reportFileRepository;
    private final ReportDownloadLogRepository reportDownloadLogRepository;

    private void insertDownloadLog(ReportFile rf, Long userId) {
        ReportDownloadLog log = new ReportDownloadLog();
        log.setReportFile(rf);
        log.setDownloadedBy(userId);
        reportDownloadLogRepository.saveAndFlush(log);
    }


    /**
     * 로컬 스토리지 루트 (기본값 report-storage)
     * 예: report-storage/2025/12/{reportId}/Report_2025-12_xxx.pdf
     */
    @Value("${com.mallapi.report.storage-path:report-storage}")
    private String storagePath;

    // =========================
    // Public APIs
    // =========================

    public ReportGenerateResult generate(ReportPrincipal principal, ReportGenerateRequest req) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (req == null || req.getFilters() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }

        // 1) 타입 검증
        ReportTypes.TypeDef type = ReportTypes.find(req.getReportTypeId());
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reportTypeId");
        }

        System.out.println("type.format=" + type.format()
                + ", req.filters.format=" + req.getFilters().getFormat());


        // 2) RBAC (report type 접근)
        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.role());
        if (type.adminOnly() && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin-only report");
        }

        // 3) format 검증 + enum 확정
        OutputFormat expectedFormat =
                (req.getFilters().getFormat() == null)
                        ? parseFormat(type.format())   // 기본값: reportType 포맷
                        : parseFormat(req.getFilters().getFormat()); // 사용자가 선택한 포맷

        String reqFormat = safeUpper(req.getFilters().getFormat());
        if (reqFormat != null && !reqFormat.equals(expectedFormat.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format mismatch");
        }

        // 4) scope 강제 + enum 확정
        DataScope scope = resolveScope(isAdmin, safeUpper(req.getFilters().getDataScope()));

        // 5) category JSON 문자열화
        String categoryJson = toJsonArray(req.getFilters().getCategory());

        // 6) job 생성 (GENERATING)
        ReportJob job = new ReportJob();
        job.setRequestedBy(principal.userId());
        job.setRoleSnapshot(principal.role());
        job.setDepartmentSnapshot(principal.departmentName()); // 안 쓰면 null이어도 OK
        job.setReportTypeId(type.id());
        job.setPeriod(req.getFilters().getPeriod());

        LocalDate[] range = toMonthRangeOrNull(job.getPeriod());
        if (range != null) {
            job.setPeriodStart(range[0]);
            job.setPeriodEnd(range[1]);
        }

        job.setDataScope(scope);
        job.setCategoryJson(categoryJson);
        job.setOutputFormat(expectedFormat);
        job.setStatus(ReportStatus.GENERATING);

        ReportJob saved = reportJobRepository.save(job);
        Long reportId = saved.getId();

        // 7) 파일 경로 준비
        Path dir = Paths.get(
                storagePath,
                String.valueOf(LocalDate.now().getYear()),
                String.valueOf(LocalDate.now().getMonthValue()),
                String.valueOf(reportId)
        );

        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            saved.setStatus(ReportStatus.FAILED);
            saved.setErrorMessage("Failed to create directories");
            reportJobRepository.save(saved);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Storage error");
        }

        String ext = (expectedFormat == OutputFormat.PDF) ? "pdf" : "xlsx";
        String fileName = buildFileName(saved.getPeriod(), type.id(), ext);
        Path outputFile = dir.resolve(fileName);

        // 8) 생성 실행 + report_file 기록 + job READY
        try {
            if (expectedFormat == OutputFormat.PDF) {
                pdfGen.generate(outputFile, saved);
            } else {
                excelGen.generate(outputFile, saved);
            }

            // 파일 메타 계산
            long size = Files.size(outputFile);
            String checksum = sha256Hex(outputFile);

            // report_file 저장 (checksum 중복이면 기존 재사용)
            ReportFile rf = saveOrReuseReportFile(saved, fileName, outputFile, expectedFormat, size, checksum);

            // job 업데이트
            saved.setStatus(ReportStatus.READY);
            saved.setFileName(rf.getFileName());
            saved.setFilePath(outputFile.toString()); // 당장은 로컬 경로 유지
            saved.setErrorMessage(null);
            reportJobRepository.save(saved);

            return new ReportGenerateResult(reportId, saved.getStatus().name(), rf.getFileName());

        } catch (Exception e) {
            saved.setStatus(ReportStatus.FAILED);
            saved.setErrorMessage(e.getMessage());
            saved.setFileName(null);
            saved.setFilePath(null);
            reportJobRepository.save(saved);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Generate failed");
        }
    }
    @Transactional
    public DownloadResult download(ReportPrincipal principal, Long reportId) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        ReportJob job = reportJobRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.role());
        if (!isAdmin && !principal.userId().equals(job.getRequestedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your report");
        }

        if (job.getStatus() != ReportStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report not ready");
        }

        try {
            ReportFile rf = reportFileRepository.findTopByReportJob_IdOrderByIdDesc(reportId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report file not found"));

            Path file = Paths.get(rf.getFileUrl());
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File missing");


            insertDownloadLog(rf, principal.userId());
            reportDownloadLogRepository.flush();
            return new DownloadResult(resource, rf.getFileName(), job.getOutputFormat());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Download error");
        }
    }


    @Transactional(readOnly = true)
    public ReportFilesResponse getFiles(ReportPrincipal principal, Long reportId) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        ReportJob job = reportJobRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        assertCanAccess(principal, job);

        var files = reportFileRepository.findByReportJob_IdOrderByCreatedAtDesc(reportId).stream()
                .map(f -> new ReportFileItem(
                        f.getId(),
                        f.getFileName(),
                        f.getFileType(),
                        f.getFileSize(),
                        f.getChecksum(),
                        f.getFileUrl(),
                        f.getCreatedAt()
                ))
                .toList();



        return new ReportFilesResponse(reportId, files);
    }

    // =========================
    // Private helpers
    // =========================

    private void assertCanAccess(ReportPrincipal principal, ReportJob job) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.role());
        if (!isAdmin && !principal.userId().equals(job.getRequestedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your report");
        }
    }

    private ReportFile saveOrReuseReportFile(
            ReportJob job,
            String fileName,
            Path outputFile,
            OutputFormat expectedFormat,
            long size,
            String checksum
    ) {
        ReportFile rf = new ReportFile();
        rf.setReportJob(job);
        rf.setFileName(fileName);
        rf.setFileUrl(outputFile.toString()); // 지금은 로컬 경로 저장
        rf.setFileType(expectedFormat.name()); // "PDF" / "EXCEL"
        rf.setFileSize(size);
        rf.setChecksum(checksum);

        try {
            return reportFileRepository.save(rf);
        } catch (DataIntegrityViolationException e) {
            // checksum UNIQUE 충돌 → 기존 파일 재사용
            return reportFileRepository.findByChecksum(checksum)
                    .orElseThrow(() -> new IllegalStateException("checksum duplicate but existing ReportFile not found"));
        }
    }

    private String buildFileName(String period, String reportTypeId, String ext) {
        String p = (period == null || period.isBlank()) ? "NA" : period.trim();
        return "Report_" + p + "_" + reportTypeId + "." + ext;
    }

    private String sha256Hex(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) > 0) md.update(buf, 0, r);
            byte[] dig = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Checksum failed");
        }
    }

    private OutputFormat parseFormat(String s) {
        String v = safeUpper(s);
        if ("PDF".equals(v)) return OutputFormat.PDF;
        if ("EXCEL".equals(v) || "XLSX".equals(v)) return OutputFormat.EXCEL;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid format in ReportTypes");
    }

    private DataScope resolveScope(boolean isAdmin, String scope) {
        if (!isAdmin) return DataScope.MY;

        // admin 기본값 DEPT (필요 시 ALL로 바꿔도 됨)
        if (scope == null) return DataScope.DEPT;

        return switch (scope) {
            case "DEPT" -> DataScope.DEPT;
            case "ALL" -> DataScope.ALL;
            case "MY" -> DataScope.MY; // 허용/차단은 정책에 따라
            default -> DataScope.DEPT;
        };
    }

    private String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private LocalDate[] toMonthRangeOrNull(String period) {
        if (period == null) return null;
        String p = period.trim();
        if (p.isEmpty()) return null;

        // MVP: "YYYY-MM"
        if (p.matches("^\\d{4}-\\d{2}$")) {
            YearMonth ym = YearMonth.parse(p);
            return new LocalDate[]{ ym.atDay(1), ym.atEndOfMonth() };
        }
        return null;
    }

    @Transactional
    public DownloadResult downloadFileById(ReportPrincipal principal, Long fileId) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        ReportFile rf = reportFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report file not found"));

        ReportJob job = rf.getReportJob(); // LAZY지만 트랜잭션 안이니까 OK
        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.role());
        if (!isAdmin && !principal.userId().equals(job.getRequestedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your report");
        }

        if (job.getStatus() != ReportStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report not ready");
        }

        try {
            Path file = Paths.get(rf.getFileUrl()); // 지금은 로컬 경로
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File missing");


            insertDownloadLog(rf, principal.userId()); // saveAndFlush로 되어 있어야 함

            return new DownloadResult(resource, rf.getFileName(), job.getOutputFormat());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Download error");
        }
    }

    @Transactional(readOnly = true)
    public ReportDownloadLogsResponse getDownloadLogsByReportId(ReportPrincipal principal, Long reportId) {
        assertAdmin(principal);


        List<ReportDownloadLogItem> items = reportDownloadLogRepository
                .findByReportFile_ReportJob_IdOrderByIdDesc(reportId)
                .stream()
                .map(this::toLogItem)
                .toList();

        return new ReportDownloadLogsResponse(reportId, items);
    }



    @Transactional(readOnly = true)
    public ReportFileDownloadLogsResponse getDownloadLogsByFileId(ReportPrincipal principal, Long fileId) {
        assertAdmin(principal);

        List<ReportDownloadLogItem> items = reportDownloadLogRepository
                .findByReportFile_IdOrderByIdDesc(fileId)
                .stream()
                .map(this::toLogItem)
                .toList();

        return new ReportFileDownloadLogsResponse(fileId, items);
    }

    private void assertAdmin(ReportPrincipal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin-only");
        }
    }

    private ReportDownloadLogItem toLogItem(ReportDownloadLog log) {
        var rf = log.getReportFile(); // ManyToOne
        return new ReportDownloadLogItem(
                log.getId(),
                rf.getId(),
                rf.getFileName(),
                log.getDownloadedBy(),
                log.getDownloadedAt()
        );
    }

    // =========================
    // Results
    // =========================

    public record ReportGenerateResult(Long reportId, String status, String fileName) {}
    public record DownloadResult(Resource resource, String fileName, OutputFormat format) {}
}
