package com.demo.controller;

import com.demo.report.entity.enums.OutputFormat;
import com.demo.report.service.ReportService;
import com.demo.security.ReportPrincipal;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report-files")
public class ReportFileController {

    private final ReportService reportService;

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadByFileId(
            @PathVariable Long fileId,
            @AuthenticationPrincipal ReportPrincipal principal
    ) {
        var r = reportService.downloadFileById(principal, fileId);

        String contentType = (r.format() == OutputFormat.PDF)
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                        java.net.URLEncoder.encode(r.fileName(), StandardCharsets.UTF_8).replace("+", "%20"))
                .contentType(MediaType.parseMediaType(contentType))
                .body((Resource) r.resource());
    }
}
