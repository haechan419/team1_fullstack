import React, { useEffect, useState } from "react";
import FetchingModal from "../../../components/common/FetchingModal";
import { getDepartments, generateReport, getReportList, downloadReport } from "../../../api/accountingApi";


const BudgetReportCenter = () => {
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [departments, setDepartments] = useState([]); // 부서 목록
  const [formData, setFormData] = useState({
    department: "", // "ALL" 또는 부서명
    startDate: "",
    endDate: "",
    format: "EXCEL", // "EXCEL" 또는 "PDF"
  });
  const [reportList, setReportList] = useState([]); // 생성된 리포트 목록

  useEffect(() => {
    loadDepartments();
    loadReportList();
  }, []);

  const loadDepartments = async () => {
    try {
      // 부서 목록 API 호출 (mallapi 패턴)
      const departmentList = await getDepartments();
      setDepartments(departmentList);
    } catch (error) {
      console.error("부서 목록 조회 실패:", error);
      // 에러 발생 시 빈 배열로 설정
      setDepartments([]);
    }
  };

  const loadReportList = async () => {
    setLoading(true);
    try {
      // 실제 API 연동 (mallapi 패턴)
      const response = await getReportList({ page: 1, size: 15 });
      
      // API 응답을 컴포넌트 형식으로 변환
      const reportData = response.dtoList.map((item) => ({
        id: item.id,
        fileName: item.fileName || `${item.reportType}_${item.id}.${item.template.toLowerCase()}`,
        createdAt: item.createdAt ? new Date(item.createdAt).toLocaleString("ko-KR") : "",
        format: item.template,
        status: item.status,
      }));
      setReportList(reportData);
    } catch (error) {
      console.error("리포트 목록 조회 실패:", error);
      setReportList([]);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateReport = async () => {
    if (!formData.startDate || !formData.endDate) {
      alert("기간을 선택해주세요.");
      return;
    }

    setGenerating(true);
    try {
      // 실제 API 연동 (mallapi 패턴)
      const reportData = {
        scopeType: formData.department === "ALL" ? "ALL" : "DEPARTMENT_NAME",
        scopeValue: formData.department === "ALL" ? null : formData.department,
        periodStart: formData.startDate,
        periodEnd: formData.endDate,
        template: formData.format,
        reportType: formData.format === "EXCEL" ? "DEPT_EXCEL" : "PERSONAL_PDF",
      };

      await generateReport(reportData);
      alert("리포트 생성이 시작되었습니다. 완료되면 목록에 표시됩니다.");
      
      // 목록 새로고침
      setTimeout(() => {
        loadReportList();
      }, 2000);
    } catch (error) {
      console.error("리포트 생성 실패:", error);
      alert("리포트 생성에 실패했습니다.");
    } finally {
      setGenerating(false);
    }
  };

  const handleDownload = async (reportId) => {
    try {
      // 실제 API 연동 (mallapi 패턴)
      const blob = await downloadReport(reportId);
      
      // Blob을 다운로드 링크로 변환
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement("a");
      link.href = url;
      
      // 리포트 정보에서 파일명 가져오기
      const report = reportList.find((r) => r.id === reportId);
      link.setAttribute("download", report?.fileName || `report_${reportId}.xlsx`);
      
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("다운로드 실패:", error);
      alert("다운로드에 실패했습니다.");
    }
  };

  return (
    <div className="budget-report-center">
      {(loading || generating) && <FetchingModal />}

      {/* 리포트 생성 폼 */}
      <div className="panel" style={{ marginBottom: "24px" }}>
        <div className="section-title">예산 리포트 생성</div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
          <div>
            <label style={{ display: "block", marginBottom: "8px", fontWeight: "600" }}>
              부서 선택
            </label>
            <select
              className="form-select"
              value={formData.department}
              onChange={(e) => setFormData({ ...formData, department: e.target.value })}
              style={{ width: "100%", padding: "8px 12px", border: "1px solid #d1d5db", borderRadius: "4px" }}
            >
              <option value="ALL">전체</option>
              {departments.map((dept) => (
                <option key={dept} value={dept}>
                  {dept}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label style={{ display: "block", marginBottom: "8px", fontWeight: "600" }}>
              파일 양식
            </label>
            <select
              className="form-select"
              value={formData.format}
              onChange={(e) => setFormData({ ...formData, format: e.target.value })}
              style={{ width: "100%", padding: "8px 12px", border: "1px solid #d1d5db", borderRadius: "4px" }}
            >
              <option value="EXCEL">Excel (.xlsx)</option>
              <option value="PDF">PDF (.pdf)</option>
            </select>
          </div>

          <div>
            <label style={{ display: "block", marginBottom: "8px", fontWeight: "600" }}>
              시작일
            </label>
            <input
              type="date"
              className="form-input"
              value={formData.startDate}
              onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
              style={{ width: "100%", padding: "8px 12px", border: "1px solid #d1d5db", borderRadius: "4px" }}
            />
          </div>

          <div>
            <label style={{ display: "block", marginBottom: "8px", fontWeight: "600" }}>
              종료일
            </label>
            <input
              type="date"
              className="form-input"
              value={formData.endDate}
              onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
              style={{ width: "100%", padding: "8px 12px", border: "1px solid #d1d5db", borderRadius: "4px" }}
            />
          </div>
        </div>

        <div style={{ marginTop: "20px", display: "flex", justifyContent: "flex-end" }}>
          <button
            className="btn btn-primary"
            onClick={handleGenerateReport}
            disabled={generating}
            style={{
              padding: "10px 24px",
              background: "#3b82f6",
              color: "white",
              border: "none",
              borderRadius: "4px",
              fontWeight: "600",
              cursor: generating ? "not-allowed" : "pointer",
              opacity: generating ? 0.6 : 1,
            }}
          >
            {generating ? "생성 중..." : "📄 RPA 보고서 생성"}
          </button>
        </div>
      </div>

      {/* 생성된 리포트 목록 */}
      <div className="panel">
        <div className="section-title">생성된 리포트 목록</div>
        {reportList.length === 0 ? (
          <div style={{ textAlign: "center", padding: "40px", color: "#6b7280" }}>
            생성된 리포트가 없습니다.
          </div>
        ) : (
          <table className="dashboard-table">
            <thead>
              <tr>
                <th>파일명</th>
                <th>생성일시</th>
                <th>형식</th>
                <th>상태</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              {reportList.map((report) => (
                <tr key={report.id}>
                  <td>{report.fileName}</td>
                  <td>{report.createdAt}</td>
                  <td>
                    <span style={{ fontWeight: "600" }}>
                      {report.format === "EXCEL" ? "📊 Excel" : "📄 PDF"}
                    </span>
                  </td>
                  <td>
                    <span
                      style={{
                        color: report.status === "DONE" ? "#10b981" : "#f59e0b",
                        fontWeight: "600",
                      }}
                    >
                      {report.status === "DONE" ? "✅ 완료" : "⏳ 생성 중"}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-primary"
                      onClick={() => handleDownload(report.id)}
                      disabled={report.status !== "DONE"}
                      style={{
                        padding: "6px 12px",
                        background: report.status === "DONE" ? "#3b82f6" : "#9ca3af",
                        color: "white",
                        border: "none",
                        borderRadius: "4px",
                        fontSize: "13px",
                        cursor: report.status === "DONE" ? "pointer" : "not-allowed",
                      }}
                    >
                      다운로드
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default BudgetReportCenter;

