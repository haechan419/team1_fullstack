// src/pages/ReportAnalyticsPage.js
import { useEffect, useMemo, useState } from "react";
import AppLayout from "../../components/layout/AppLayout";
import "../../styles/report.css";
import { REPORT_TYPES } from "../../constants/reportTypes";

export default function ReportAnalyticsPage() {
    const API_BASE = "http://localhost:8080";
    const [role] = useState("ADMIN"); // TODO: 실제 로그인 role로 교체

    const SCOPE_MAP = { "My Data": "MY", Department: "DEPT", All: "ALL" };
    const FORMAT_MAP = { PDF: "PDF", Excel: "EXCEL", EXCEL: "EXCEL" };

    const pad2 = (n) => String(n).padStart(2, "0");
    const currentYm = () => {
        const d = new Date();
        return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}`;
    };

    const buildMonthOptions = (monthsBack = 24) => {
        const now = new Date();
        const arr = [];

        for (let i = 0; i < monthsBack; i++) {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            arr.push(`${d.getFullYear()}-${pad2(d.getMonth() + 1)}`);
        }

        return arr;
    };

    const PERIOD_OPTIONS = buildMonthOptions(24); // 최근 24개월




    const getDefaultSelected = () => ({
        period: [currentYm()],
        scope: ["Department"],
        category: ["All"],
        format: ["PDF"],
    });


    // -------------------------
    // state
    // -------------------------
    const [generatedReportId, setGeneratedReportId] = useState(null);
    const [isGenerated, setIsGenerated] = useState(false);
    const [isGenerating, setIsGenerating] = useState(false);

    const [files, setFiles] = useState([]);
    const [isFilesLoading, setIsFilesLoading] = useState(false);

    const [downloadLogs, setDownloadLogs] = useState([]);

    // -------------------------
// Schedules (ADMIN)
// -------------------------
    const [schedules, setSchedules] = useState([]);
    const [isSchedulesLoading, setIsSchedulesLoading] = useState(false);

    const fetchSchedules = async () => {
        try {
            setIsSchedulesLoading(true);
            const res = await fetch(`${API_BASE}/api/admin/report-schedules`, {
                method: "GET",
                headers: { "X-User-Id": "1", "X-Role": role }, // ✅ 지금 너 방식 그대로
                // 나중에 로그인 붙이면: Authorization: `Bearer ${localStorage.getItem("accessToken")}`
            });
            if (!res.ok) throw new Error(await res.text());
            const data = await res.json();
            setSchedules(data.items ?? []);
        } catch (e) {
            console.error(e);
            setSchedules([]);
        } finally {
            setIsSchedulesLoading(false);
        }
    };

    const [isLogsLoading, setIsLogsLoading] = useState(false);
    const [logsMode, setLogsMode] = useState({ type: "REPORT", fileId: null }); // REPORT | FILE

    // -------------------------
    // RBAC options
    // -------------------------
    const visibleReportTypes = useMemo(
        () => REPORT_TYPES.filter((rt) => rt.roles.includes(role)),
        [role]
    );

    const scopeOptions = useMemo(
        () => (role === "ADMIN" ? ["Department", "All"] : ["My Data"]),
        [role]
    );

    // -------------------------
    // Filter defs (CSS 구조 고정)
    // -------------------------
    const FILTERS = useMemo(
        () => [
            { key: "period", label: "Period", type: "single", options: PERIOD_OPTIONS },
            { key: "scope", label: "Data Scope", type: "single", options: scopeOptions },
            { key: "category", label: "Category", type: "multi", options: ["All", "Meals", "Supplies", "Taxi", "Other"] },
            { key: "format", label: "Format", type: "single", options: ["PDF", "EXCEL"] },
        ],
        [scopeOptions]
    );

    const DEFAULT_SELECTED = {
        period: ["2025-03"],
        scope: ["Department"],
        category: ["All"],
        format: ["PDF"],
    };



    const [activeKey, setActiveKey] = useState(null);
    const [filterSearch, setFilterSearch] = useState({ period: "", scope: "", category: "", format: "" });

    const [selected, setSelected] = useState(getDefaultSelected());


    useEffect(() => {
        if (role === "ADMIN") fetchSchedules();
    }, [role]);


    useEffect(() => {
        if (role !== "ADMIN") {
            setSelected((p) => ({ ...p, scope: ["My Data"] }));
        }
    }, [role]);

    // ReportType
    const [reportTypeId, setReportTypeId] = useState(() => visibleReportTypes[0]?.id ?? "");
    useEffect(() => {
        if (!visibleReportTypes.some((r) => r.id === reportTypeId)) {
            setReportTypeId(visibleReportTypes[0]?.id ?? "");
        }
    }, [visibleReportTypes]); // eslint-disable-line

    // -------------------------
    // helpers
    // -------------------------
    const toggleActive = (key) => setActiveKey((p) => (p === key ? null : key));

    const onPickSingle = (key, value) => {
        setSelected((p) => ({ ...p, [key]: [value] }));
        setActiveKey(null);
    };

    const onToggleMulti = (key, value) => {
        setSelected((p) => {
            const cur = p[key] ?? [];
            let next = cur.includes(value) ? cur.filter((v) => v !== value) : [...cur, value];


            if (key === "category") {
                if (value === "All") {
                    // All을 켰으면 All만 남김
                    next = ["All"];
                } else {
                    // 다른 걸 켰으면 All 제거
                    next = next.filter((x) => x !== "All");
                    // 다 꺼져서 0개면 All 복구
                    if (next.length === 0) next = ["All"];
                }
            }
            return { ...p, [key]: next };
        });
    };


    const removeChip = (key, value) => {
        setSelected((p) => {
            const cur = p[key] ?? [];
            const next = cur.filter((x) => x !== value);

            const def = FILTERS.find((f) => f.key === key);

            // ✅ single은 0개가 되면 "없음"이 아니라 기본값으로 복구
            if (def?.type === "single") {
                if (next.length === 0) {
                    return { ...p, [key]: DEFAULT_SELECTED[key] ?? [] };
                }
                return { ...p, [key]: next };
            }

            // ✅ multi(category)는 0개가 되면 All로 복구 (ERP 필터 안정화)
            if (def?.type === "multi") {
                if (next.length === 0) {
                    return { ...p, [key]: DEFAULT_SELECTED[key] ?? ["All"] };
                }

                // (선택사항) All 정책: 다른 거 선택되면 All 제거
                if (key === "category") {
                    const cleaned = next.includes("All") && next.length > 1
                        ? next.filter((x) => x !== "All")
                        : next;
                    return { ...p, [key]: cleaned };
                }

                return { ...p, [key]: next };
            }

            return { ...p, [key]: next };
        });
    };


    const formatBytes = (bytes) => {
        const n = Number(bytes ?? 0);
        if (!n) return "0 B";
        if (n < 1024) return `${n} B`;
        if (n < 1024 * 1024) return `${Math.round(n / 1024)} KB`;
        return `${(n / (1024 * 1024)).toFixed(1)} MB`;
    };

    // -------------------------
    // Preview data (메타만)
    // -------------------------
    const preview = useMemo(() => {
        const rt = visibleReportTypes.find((r) => r.id === reportTypeId);
        const period = selected.period?.[0] ?? "-";
        const scope = selected.scope?.[0] ?? "-";
        const format = rt?.outputFormat ?? selected.format?.[0] ?? "-";
        return {
            reportTypeLabel: rt?.label ?? "-",
            recordsIncluded: rt?.preview?.recordsIncluded ?? "-",
            totalAmount: rt?.preview?.totalAmount ?? "-",
            period,
            scope,
            outputFormat: format,
            viewMode: (FORMAT_MAP[format] ?? "PDF") === "EXCEL" ? "EXCEL" : "PDF",
        };
    }, [visibleReportTypes, reportTypeId, selected]);

    // -------------------------
    // API: Generate
    // -------------------------
    const handleGenerate = async () => {

        try {
            setIsGenerating(true);

            const rt = visibleReportTypes.find((r) => r.id === reportTypeId);
            const uiScope = selected.scope?.[0] ?? (role === "ADMIN" ? "Department" : "My Data");

            // 기존 rt.outputFormat 기반 로직 삭제

            const uiFormat = selected.format?.[0] ?? "PDF";  // Filter에서 고른 값
            const format = uiFormat.trim().toUpperCase() === "EXCEL" ? "EXCEL" : "PDF";

            const payload = {
                reportTypeId,
                    filters: {
                        period: selected.period?.[0] ?? null,
                        dataScope: SCOPE_MAP[uiScope] ?? (role === "ADMIN" ? "DEPT" : "MY"),
                        category: selected.category?.length ? selected.category : ["ALL"],
                        format,


                },
            };



            const res = await fetch(`${API_BASE}/api/reports/generate`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": "1",
                    "X-Role": role,
                    // ⚠️ 한글 헤더 금지: X-Dept 같은 건 넣지 말기
                },
                body: JSON.stringify(payload),
            });

            if (!res.ok) throw new Error(await res.text());

            const data = await res.json();
            setGeneratedReportId(data.reportId);
            setIsGenerated(true);
            setLogsMode({ type: "REPORT", fileId: null });
        } catch (e) {
            console.error(e);
            alert("Generate failed");
            setGeneratedReportId(null);
            setIsGenerated(false);
        } finally {
            setIsGenerating(false);
        }
    };

    // -------------------------
    // API: Fetch files
    // -------------------------
    useEffect(() => {
        if (!generatedReportId) {
            setFiles([]);
            return;
        }

        (async () => {
            try {
                setIsFilesLoading(true);
                const res = await fetch(`${API_BASE}/api/reports/${generatedReportId}/files`, {
                    method: "GET",
                    headers: { "X-User-Id": "1", "X-Role": role },
                });
                if (!res.ok) throw new Error(await res.text());
                const data = await res.json();
                const list = Array.isArray(data) ? data : data.files;
                setFiles(list ?? []);
            } catch (e) {
                console.error(e);
                setFiles([]);
            } finally {
                setIsFilesLoading(false);
            }
        })();
    }, [generatedReportId, role]);

    // -------------------------
    // Download (대표 / 개별)
    // -------------------------
    const downloadBlob = async (res, fallbackName) => {
        const cd = res.headers.get("Content-Disposition") || "";
        let filename = fallbackName;
        const m = cd.match(/filename\*\=UTF-8''([^;]+)/i);
        if (m?.[1]) filename = decodeURIComponent(m[1]);

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    };

    const handleDownload = async () => {
        if (!generatedReportId) return;
        const res = await fetch(`${API_BASE}/api/reports/${generatedReportId}/download`, {
            method: "GET",
            headers: { "X-User-Id": "1", "X-Role": role },
        });
        if (!res.ok) return alert("Download failed");
        await downloadBlob(res, "report");
        // 로그 갱신
        if (role === "ADMIN") fetchLogsByReport(generatedReportId);
    };

    const handleDownloadFile = async (fileId, fileName) => {
        const res = await fetch(`${API_BASE}/api/report-files/${fileId}/download`, {
            method: "GET",
            headers: { "X-User-Id": "1", "X-Role": role },
        });
        if (!res.ok) return alert("Download failed");
        await downloadBlob(res, fileName || "report");
        if (role === "ADMIN") {
            if (logsMode.type === "FILE") fetchLogsByFile(fileId);
            else if (generatedReportId) fetchLogsByReport(generatedReportId);
        }
    };

    // -------------------------
    // Logs (A/B)
    // ⚠️ 아래 URL은 너 백엔드 매핑에 맞춰 쓰면 됨.
    // 내가 이전에 줬던 /download-logs 형태로 맞춰둠.
    // -------------------------
    const fetchLogsByReport = async (reportId) => {
        if (!reportId) return;
        try {
            setIsLogsLoading(true);
            setLogsMode({ type: "REPORT", fileId: null });

            const res = await fetch(`${API_BASE}/api/reports/${reportId}/downloads`, {
                method: "GET",
                headers: { "X-User-Id": "1", "X-Role": role },
            });
            if (!res.ok) throw new Error(await res.text());

            const data = await res.json();
            const list =
                Array.isArray(data) ? data :
                    (data.items ?? data.logs ?? data.result ?? data.data ?? []);
            setDownloadLogs(list ?? []);
        } catch (e) {
            console.error(e);
            setDownloadLogs([]);
        } finally {
            setIsLogsLoading(false);
        }
    };

    const fetchLogsByFile = async (fileId) => {
        if (!fileId) return;
        try {
            setIsLogsLoading(true);
            setLogsMode({ type: "FILE", fileId });

            const res = await fetch(`${API_BASE}/api/report-files/${fileId}/downloads`, {
                method: "GET",
                headers: { "X-User-Id": "1", "X-Role": role },
            });
            if (!res.ok) throw new Error(await res.text());

            const data = await res.json();
            const list =
                Array.isArray(data) ? data :
                    data.logs ?? data.items ?? data.result ?? data.data ?? [];
            setDownloadLogs(list ?? []);
        } catch (e) {
            console.error(e);
            setDownloadLogs([]);
        } finally {
            setIsLogsLoading(false);
        }
    };

    // Generate 후 ADMIN이면 기본 로그 로드
    useEffect(() => {
        if (role === "ADMIN" && generatedReportId) fetchLogsByReport(generatedReportId);
        if (!generatedReportId) setDownloadLogs([]);
    }, [generatedReportId, role]);

    // -------------------------
    // render
    // -------------------------
    return (
        <AppLayout>
            <div className="report-page">
                <div className="page-title">Report &amp; Analytics</div>

                {/* 1) Filter Panel */}
                <section className="section">
                    <div className="section-title">Filter Panel</div>

                    <div className="panel panel-filter2">
                        <div className="filter-tabs">
                            {FILTERS.map((f) => (
                                <button
                                    key={f.key}
                                    type="button"
                                    className={`filter-tab ${activeKey === f.key ? "is-active" : ""}`}
                                    onClick={() => toggleActive(f.key)}
                                >
                                    {f.label} <span className="caret">▾</span>
                                </button>
                            ))}
                        </div>

                        {/* chips */}
                        <div className="chips-row" style={{ display: "flex", alignItems: "center", gap: 12 }}>
                            <div className="filter-chips" style={{ display: "flex", gap: 10, flexWrap: "wrap", flex: 1 }}>
                                {FILTERS.flatMap((f) =>
                                        (selected[f.key] ?? []).map((v) => (
                                            <span className="chip" key={`${f.key}-${v}`}>
                                                    {f.label}: {v}
                                                <button className="chip-x" type="button" onClick={() => removeChip(f.key, v)}>
                                                     ×
                                                 </button>
                                            </span>
                                        ))
                                )}
                            </div>

                            <button
                                type="button"
                                className="action-btn action-secondary"
                                onClick={() => setSelected(getDefaultSelected())}
                            >
                                Clear Filters
                            </button>
                        </div>


                        {/* dropdown */}
                        {activeKey && (
                            <div className="filter-dropdown">
                                <div className="dropdown-inner">
                                    {/* search row (CSS에서 grid-column 전체폭) */}
                                    <div className="dropdown-search-row">
                                        <input
                                            className="dropdown-search"
                                            placeholder="Search..."
                                            value={filterSearch[activeKey] || ""}
                                            onChange={(e) => setFilterSearch((p) => ({ ...p, [activeKey]: e.target.value }))}
                                        />
                                    </div>

                                    {/* options grid */}
                                    <div className="dropdown-options">
                                        {(() => {
                                            const def = FILTERS.find((x) => x.key === activeKey);
                                            const keyword = (filterSearch[activeKey] || "").toLowerCase();
                                            const list = (def?.options ?? []).filter((opt) => opt.toLowerCase().includes(keyword));

                                            if (list.length === 0) return <div className="dropdown-empty">No results</div>;

                                            if (def?.type === "single") {
                                                return list.map((opt) => {
                                                    const checked = (selected[def.key]?.[0] ?? "") === opt;
                                                    const isDisabled = def.key === "scope" && role !== "ADMIN" && opt !== "My Data";
                                                    return (
                                                        <label
                                                            key={opt}
                                                            className={`dropdown-item ${isDisabled ? "is-disabled" : ""}`}
                                                            title={isDisabled ? "Employee scope is fixed" : ""}
                                                        >
                                                            <input
                                                                type="radio"
                                                                name={`dd-${def.key}`}
                                                                checked={checked}
                                                                disabled={isDisabled}
                                                                onChange={() => onPickSingle(def.key, opt)}
                                                            />
                                                            {opt}
                                                        </label>
                                                    );
                                                });
                                            }

                                            // multi
                                            return list.map((opt) => {
                                                const checked = (selected[def.key] ?? []).includes(opt);
                                                return (
                                                    <label key={opt} className="dropdown-item">
                                                        <input
                                                            type="checkbox"
                                                            checked={checked}
                                                            onChange={() => onToggleMulti(def.key, opt)}
                                                        />
                                                        {opt}
                                                    </label>
                                                );
                                            });
                                        })()}
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </section>

                {/* 2) Report Type Selection */}
                <section className="section">
                    <div className="section-title">Report Type Selection</div>

                    <div className="panel">
                        <div className="radio-list">
                            {visibleReportTypes.map((rt) => (
                                <label className="radio-row" key={rt.id}>
                                    <input
                                        type="radio"
                                        name="reportType"
                                        checked={reportTypeId === rt.id}
                                        onChange={() => setReportTypeId(rt.id)}
                                    />
                                    <span>{rt.label}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                </section>

                {/* 3) Preview (doc-viewer + pdf/excel mock) */}
                <section className="section">
                    <div className="section-title">Report Summary Preview</div>

                    <div className="panel preview-box">
                        <div className="doc-viewer">
                            {preview.viewMode === "PDF" ? (
                                <div className="doc-pdf">
                                    <div className="pdf-page">
                                        <div className="pdf-header">
                                            <div className="pdf-title">Report Summary</div>
                                            <div className="pdf-sub">Preview</div>
                                        </div>

                                        <div className="pdf-body">
                                            {[
                                                ["Report Type", preview.reportTypeLabel],
                                                ["Records Included", preview.recordsIncluded],
                                                ["Total Amount", preview.totalAmount],
                                                ["Period", preview.period],
                                                ["Scope", preview.scope],
                                                ["Output Format", preview.outputFormat],
                                            ].map(([k, v]) => (
                                                <div className="pdf-row" key={k}>
                                                    <div className="k">{k}</div>
                                                    <div className="v">{v}</div>
                                                </div>
                                            ))}

                                            <div className="pdf-hr" />
                                            <div className="pdf-paragraph">
                                                This is a metadata preview (ERP-style verification) before generating the actual document.
                                            </div>
                                        </div>

                                        <div className="pdf-footer">
                                            <span>Confidential</span>
                                            <span>Page 1</span>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className="doc-excel">
                                    <div className="excel-topbar">
                                        <span className="excel-pill">EXCEL</span>
                                        <span className="excel-meta">Preview grid</span>
                                    </div>

                                    <div className="excel-grid">
                                        <div className="excel-corner" />
                                        {["A", "B", "C", "D", "E", "F"].map((c) => (
                                            <div key={c} className="excel-colhead">{c}</div>
                                        ))}

                                        {[1, 2, 3, 4, 5, 6].map((r) => (
                                            <div key={r} className="excel-row">
                                                <div className="excel-rowhead">{r}</div>
                                                <div className="excel-cell keycell">{r === 1 ? "Key" : ""}</div>
                                                <div className="excel-cell">{r === 1 ? "Value" : ""}</div>
                                                <div className="excel-cell muted" />
                                                <div className="excel-cell muted" />
                                                <div className="excel-cell muted" />
                                                <div className="excel-cell muted" />
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </section>

                {/* 4) Actions */}
                <section className="section section-actions">
                    <div className="actions-row">
                        <button type="button" className="action-btn action-secondary">
                            Export
                        </button>

                        <button
                            type="button"
                            className="action-btn action-primary"
                            onClick={handleGenerate}
                            disabled={isGenerating}
                        >
                            {isGenerating ? "Generating..." : "Generate Report"}
                        </button>

                        <button
                            type="button"
                            className={`action-btn action-primary2 ${!isGenerated ? "is-disabled" : ""}`}
                            onClick={handleDownload}
                            disabled={!isGenerated}
                        >
                            Download
                        </button>
                    </div>


                    <div className="actions-hint">
                        {isGenerated ? "Generate 완료. Download 가능." : "Generate 이후 Download 활성화."}
                    </div>
                </section>
                {generatedReportId && (
                    <div className="panel" style={{ marginTop: 12 }}>
                        <div className="hint" style={{ marginBottom: 8 }}>
                            Available files
                        </div>

                        {isFilesLoading ? (
                            <div className="hint">Loading files...</div>
                        ) : files.length === 0 ? (
                            <div className="hint">No files generated yet.</div>
                        ) : (
                            <ul className="fileList">
                                {files.map((f) => (
                                    <li key={f.fileId ?? f.id} className="fileRow">
                                        <div className="fileMeta">
                                            <div className="fileName">{f.fileName}</div>
                                            <div className="fileSub">
                                                {f.fileType} · {formatBytes(f.fileSize)} ·{" "}
                                                {f.createdAt ? new Date(f.createdAt).toLocaleString() : ""}
                                            </div>
                                        </div>

                                        {/* 개별 다운로드는 남겨도 되고, 빼도 됨 */}
                                        {/*<button*/}
                                        {/*    type="button"*/}
                                        {/*    className="action-btn action-primary2"*/}
                                        {/*    onClick={() => handleDownloadFile(f.fileId ?? f.id, f.fileName)}*/}
                                        {/*>*/}
                                        {/*    Download*/}
                                        {/*</button>*/}

                                        {role === "ADMIN" && (
                                            <button
                                                type="button"
                                                className="action-btn action-secondary"
                                                onClick={() => fetchLogsByFile(f.fileId ?? f.id)}
                                            >
                                                Logs
                                            </button>
                                        )}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                )}

                {/* 5) Automation / Schedules (ADMIN only) */}
                {role === "ADMIN" && (
                    <section className="section">
                        <div className="section-title">Automation / Schedules</div>

                        <div className="panel">
                            <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 10 }}>
                                <button
                                    type="button"
                                    className="action-btn action-secondary"
                                    onClick={fetchSchedules}
                                    disabled={isSchedulesLoading}
                                >
                                    {isSchedulesLoading ? "Refreshing..." : "Refresh"}
                                </button>
                            </div>

                            {isSchedulesLoading ? (
                                <div className="hint">Loading schedules...</div>
                            ) : schedules.length === 0 ? (
                                <div className="hint">No schedules.</div>
                            ) : (
                                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                                    <thead>
                                    <tr style={{ textAlign: "left", opacity: 0.75 }}>
                                        <th style={{ padding: "8px 6px" }}>Name</th>
                                        <th style={{ padding: "8px 6px" }}>Type</th>
                                        <th style={{ padding: "8px 6px" }}>Scope</th>
                                        <th style={{ padding: "8px 6px" }}>Format</th>
                                        <th style={{ padding: "8px 6px" }}>Enabled</th>
                                        <th style={{ padding: "8px 6px" }}>Next Run</th>
                                        <th style={{ padding: "8px 6px" }}>Last Run</th>
                                        <th style={{ padding: "8px 6px" }}>Last Job</th>
                                        <th style={{ padding: "8px 6px" }}>Fail</th>
                                        <th style={{ padding: "8px 6px" }}>Last Error</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {schedules.map((s) => (
                                        <tr key={s.id}>
                                            <td style={{ padding: "8px 6px" }}>{s.name}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.reportTypeId}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.dataScope}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.outputFormat}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.isEnabled ? "ON" : "OFF"}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.nextRunAt ? new Date(s.nextRunAt).toLocaleString() : "-"}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.lastRunAt ? new Date(s.lastRunAt).toLocaleString() : "-"}</td>
                                            <td style={{ padding: "8px 6px" }}>
                                                {s.lastJobId ? (
                                                    <a href={`${API_BASE}/api/reports/${s.lastJobId}/download`} target="_blank" rel="noreferrer">
                                                        download
                                                    </a>
                                                ) : (
                                                    "-"
                                                )}
                                            </td>
                                            <td style={{ padding: "8px 6px" }}>{s.failCount ?? 0}</td>
                                            <td style={{ padding: "8px 6px" }}>{s.lastError ? String(s.lastError).slice(0, 120) : "-"}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </section>
                )}




                {/* 6) Download History (ADMIN only) */}
                {role === "ADMIN" && (
                    <section className="section">
                        <div className="section-title">Download History</div>

                        <div className="panel">
                            {!generatedReportId ? (
                                <div className="hint">Generate 후 확인 가능</div>
                            ) : isLogsLoading ? (
                                <div className="hint">Loading logs...</div>
                            ) : downloadLogs.length === 0 ? (
                                <div className="hint">No logs yet.</div>
                            ) : (
                                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                                    <thead>
                                    <tr style={{ textAlign: "left", opacity: 0.75 }}>
                                        <th style={{ padding: "8px 6px" }}>ID</th>
                                        <th style={{ padding: "8px 6px" }}>ReportFile ID</th>
                                        <th style={{ padding: "8px 6px" }}>Downloaded By</th>
                                        <th style={{ padding: "8px 6px" }}>Downloaded At</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {downloadLogs.map((log) => {
                                        const id = log.logId ?? log.id;
                                        const fileId = log.fileId ?? log.reportFileId;
                                        const who = log.downloadedBy ?? log.download_by;
                                        const at = log.downloadedAt ?? log.downloaded_at;

                                        return (
                                            <tr key={id ?? `${fileId}-${at}`}>
                                                <td style={{ padding: "8px 6px" }}>{id ?? "-"}</td>
                                                <td style={{ padding: "8px 6px" }}>{fileId ?? "-"}</td>
                                                <td style={{ padding: "8px 6px" }}>{who ?? "-"}</td>
                                                <td style={{ padding: "8px 6px" }}>
                                                    {at ? new Date(at).toLocaleString() : "-"}
                                                </td>
                                            </tr>
                                        );
                                    })}

                                    </tbody>
                                </table>
                            )}

                            {generatedReportId && (
                                <div style={{ marginTop: 12, textAlign: "right" }}>
                                    <button
                                        type="button"
                                        className="action-btn action-secondary"
                                        onClick={() => fetchLogsByReport(generatedReportId)}
                                        disabled={!generatedReportId}
                                    >
                                        View All Logs
                                    </button>

                                    {logsMode.type === "FILE" && (
                                        <button
                                            type="button"
                                            className="action-btn action-secondary"
                                            style={{ marginLeft: 10 }}
                                            onClick={() => fetchLogsByReport(generatedReportId)}
                                        >
                                            Back to Report Logs
                                        </button>
                                    )}
                                </div>
                            )}
                        </div>
                    </section>
                )}
            </div>
        </AppLayout>
    );
}
