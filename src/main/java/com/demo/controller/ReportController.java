package com.demo.controller;

import com.demo.report.dto.*;
import com.demo.report.entity.enums.OutputFormat;
import com.demo.report.service.ReportService;
import com.demo.security.ReportPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<ReportGenerateResponse> generate(
            @AuthenticationPrincipal ReportPrincipal principal,
            @RequestBody @Valid ReportGenerateRequest req
    ) {
        var r = reportService.generate(principal, req);
        return ResponseEntity.ok(new ReportGenerateResponse(r.reportId(), r.status(), r.fileName()));
    }
    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal ReportPrincipal principal,
            @PathVariable Long reportId
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        var d = reportService.download(principal, reportId);

        var format = d.format(); // OutputFormat enum

        String contentType = (format == OutputFormat.PDF)
                ? MediaType.APPLICATION_PDF_VALUE
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String encoded = java.net.URLEncoder
                .encode(d.fileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(d.resource());
    }

    @GetMapping("/{reportId}/files")
    public ResponseEntity<ReportFilesResponse> files(
            @AuthenticationPrincipal ReportPrincipal principal,
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(reportService.getFiles(principal, reportId));
    }

    @GetMapping("/api/report-files/{fileId}/download")
    public ResponseEntity<Resource> downloadByFileId(
            @PathVariable Long fileId,
            @AuthenticationPrincipal ReportPrincipal principal
    ) {
        ReportService.DownloadResult r = reportService.downloadFileById(principal, fileId);

        String contentType = (r.format() == OutputFormat.PDF)
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + r.fileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(r.resource());
    }

    @GetMapping("/api/reports/{reportId}/downloads")
    public ReportDownloadLogsResponse downloadLogsByReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal ReportPrincipal principal
    ) {
        return reportService.getDownloadLogsByReportId(principal, reportId);
    }

    @GetMapping("/api/report-files/{fileId}/downloads")
    public ReportFileDownloadLogsResponse downloadLogsByFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal ReportPrincipal principal
    ) {
        return reportService.getDownloadLogsByFileId(principal, fileId);
    }






    @GetMapping("/debug/me")
    public Object me(@AuthenticationPrincipal Object principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return java.util.Map.of(
                "principalParam", principal,
                "principalParamClass", principal == null ? null : principal.getClass().getName(),
                "auth", auth == null ? null : auth.toString(),
                "authPrincipalClass", auth == null ? null : auth.getPrincipal().getClass().getName(),
                "authorities", auth == null ? null : auth.getAuthorities()
        );
    }

}
