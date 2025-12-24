import React, { useEffect, useState } from "react";
import "../../../styles/layout.css";
import "../../../styles/dashboard.css";
import FetchingModal from "../../../components/common/FetchingModal";
import {
  getDepartmentStatistics,
  getCategoryStatistics,
  getSummary,
  getOverBudgetList,
} from "../../../api/accountingApi";

const StatisticsDashboard = () => {
  const [loading, setLoading] = useState(false);
  const [summary, setSummary] = useState({
    totalBudgetExecutionRate: 0, // 부서 총 예산 집행률 (%)
    todayPendingCount: 0, // 오늘의 미결재 건수
    monthlyTotalExpense: 0, // 이번 달 총 지출액
    overBudgetCount: 0, // 예산 초과 주의 인원 수
  });
  const [departmentChart, setDepartmentChart] = useState([]); // 부서별 지출
  const [categoryChart, setCategoryChart] = useState([]); // 항목별 지출
  const [overBudgetList, setOverBudgetList] = useState([]); // 예산 초과 주의 인원 리스트

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    setLoading(true);
    try {
      // 전체 통계 요약 API 호출
      const summaryResponse = await getSummary();
      setSummary({
        totalBudgetExecutionRate: summaryResponse.totalBudgetExecutionRate || 0,
        todayPendingCount: summaryResponse.todayPendingCount || 0,
        monthlyTotalExpense: summaryResponse.monthlyTotalExpense || 0,
        overBudgetCount: summaryResponse.overBudgetCount || 0,
      });

      // 부서별 통계 API 호출
      const departmentResponse = await getDepartmentStatistics({ status: "APPROVED" });
      const departmentData = departmentResponse.map((item) => ({
        name: item.departmentName || "기타",
        amount: item.totalAmount || 0,
      }));
      setDepartmentChart(departmentData);

      // 카테고리별 통계 API 호출
      const categoryResponse = await getCategoryStatistics({ status: "APPROVED" });
      setCategoryChart(categoryResponse);

      // 예산 초과 인원 리스트 API 호출
      const overBudgetResponse = await getOverBudgetList();
      setOverBudgetList(overBudgetResponse);
    } catch (error) {
      console.error("통계 조회 실패:", error);
      // 에러 발생 시 빈 배열로 설정
      setDepartmentChart([]);
      setCategoryChart([]);
      setOverBudgetList([]);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: "KRW",
    }).format(amount);
  };

  return (
    <div className="statistics-dashboard">
      {loading && <FetchingModal />}

      {/* 상단 통계 카드 */}
      <div className="dashboard-grid">
        <div className="panel stat-card">
          <div>
            <div className="stat-title">부서 총 예산 집행률</div>
            <div className="stat-value">{summary.totalBudgetExecutionRate}%</div>
          </div>
          <div className="stat-footer">
            <span className={summary.totalBudgetExecutionRate >= 80 ? "trend-up" : "trend-down"}>
              {summary.totalBudgetExecutionRate >= 80 ? "⚠️ 주의" : "✅ 안정적"}
            </span>
            <span className="text-muted"> 전월 대비</span>
          </div>
        </div>

        <div className="panel stat-card">
          <div>
            <div className="stat-title">오늘의 미결재 건수</div>
            <div className="stat-value">{summary.todayPendingCount}건</div>
          </div>
          <div className="stat-footer">
            <span className="trend-up">▲ 3건</span>
            <span className="text-muted"> 어제 대비</span>
          </div>
        </div>

        <div className="panel stat-card">
          <div>
            <div className="stat-title">이번 달 총 지출액</div>
            <div className="stat-value">{formatCurrency(summary.monthlyTotalExpense)}</div>
          </div>
          <div className="stat-footer">
            <span className="trend-up">▲ 10%</span>
            <span className="text-muted"> 전월 대비</span>
          </div>
        </div>

        <div className="panel stat-card">
          <div>
            <div className="stat-title">예산 초과 주의 인원</div>
            <div className="stat-value">{summary.overBudgetCount}명</div>
          </div>
          <div className="stat-footer text-muted">80% 이상 소진</div>
        </div>
      </div>

      {/* 중앙 차트 영역 */}
      <div className="dashboard-row">
        <div className="panel" style={{ minHeight: "400px" }}>
          <div className="section-title">부서별 지출 비중</div>
          <div
            style={{
              height: "320px",
              background: "rgba(17,24,39,0.02)",
              borderRadius: "12px",
              display: "grid",
              placeItems: "center",
              color: "var(--text-muted)",
              fontWeight: "700",
            }}
          >
            [도넛 차트 영역 - Chart.js 예정]
            <div style={{ marginTop: "20px", fontSize: "14px" }}>
              {departmentChart.map((dept, idx) => (
                <div key={idx} style={{ marginBottom: "8px" }}>
                  {dept.name}: {formatCurrency(dept.amount)}
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="panel">
          <div className="section-title">항목별 지출 비중</div>
          <div
            style={{
              height: "320px",
              background: "rgba(17,24,39,0.02)",
              borderRadius: "12px",
              display: "grid",
              placeItems: "center",
              color: "var(--text-muted)",
              fontWeight: "700",
            }}
          >
            [바 차트 영역 - Chart.js 예정]
            <div style={{ marginTop: "20px", fontSize: "14px" }}>
              {categoryChart.map((cat, idx) => (
                <div key={idx} style={{ marginBottom: "8px" }}>
                  {cat.name}: {formatCurrency(cat.amount)}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* 하단 예산 초과 주의 인원 리스트 */}
      <div className="panel" style={{ marginTop: "24px" }}>
        <div className="section-title">예산 초과 주의 인원 리스트</div>
        <table className="dashboard-table">
          <thead>
            <tr>
              <th>사원명</th>
              <th>부서</th>
              <th>예산 소진율</th>
              <th>잔여 예산</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {overBudgetList.map((person, idx) => (
              <tr key={idx}>
                <td>{person.name}</td>
                <td>{person.department}</td>
                <td>
                  <span style={{ fontWeight: "700" }}>{person.executionRate}%</span>
                </td>
                <td>{formatCurrency(person.remaining)}</td>
                <td>
                  <span
                    style={{
                      color: person.executionRate >= 80 ? "#e11d48" : "#f59e0b",
                      fontWeight: "600",
                    }}
                  >
                    {person.executionRate >= 80 ? "⚠️ 주의" : "⚡ 경고"}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default StatisticsDashboard;

