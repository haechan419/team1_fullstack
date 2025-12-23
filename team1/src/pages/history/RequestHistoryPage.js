import React, { useState } from "react";
import AppLayout from "../../components/layout/AppLayout";
import { useCart } from "../../context/CartContext";
import "../../styles/history.css";

export default function RequestHistoryPage() {
  const { requests } = useCart();
  const [filter, setFilter] = useState("ALL"); // ALL, pending, approved, rejected
  const [expandedId, setExpandedId] = useState(null); // 어떤 항목을 펼쳤는지

  // 필터링 로직
  const filteredRequests = requests.filter((req) =>
    filter === "ALL" ? true : req.status === filter
  );

  // 통계 계산
  const stats = {
    total: requests.length,
    pending: requests.filter((r) => r.status === "pending").length,
    approved: requests.filter((r) => r.status === "approved").length,
    rejected: requests.filter((r) => r.status === "rejected").length,
  };

  // 상세 내용 펼치기/접기 토글
  const toggleExpand = (id) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  return (
    <AppLayout>
      <div className="page-header">
        <h2 className="page-title">📂 구매 신청 내역</h2>
        <p className="text-gray">
          상신한 비품 구매 요청의 진행 상황을 상세하게 확인합니다.
        </p>
      </div>

      <div className="history-container">
        {/* 1. 상단 통계 카드 */}
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-label">총 신청 건수</div>
            <div className="stat-value">{stats.total}건</div>
          </div>
          <div className="stat-card pending">
            <div className="stat-label">대기 중</div>
            <div className="stat-value">{stats.pending}건</div>
          </div>
          <div className="stat-card approved">
            <div className="stat-label">승인 완료</div>
            <div className="stat-value">{stats.approved}건</div>
          </div>
          <div className="stat-card rejected">
            <div className="stat-label">반려됨</div>
            <div className="stat-value">{stats.rejected}건</div>
          </div>
        </div>

        {/* 2. 필터 탭 */}
        <div className="filter-tabs">
          {["ALL", "pending", "approved", "rejected"].map((status) => (
            <button
              key={status}
              className={`tab-btn ${filter === status ? "active" : ""}`}
              onClick={() => setFilter(status)}
            >
              {status === "ALL"
                ? "전체 보기"
                : status === "pending"
                ? "승인 대기"
                : status === "approved"
                ? "승인 완료"
                : "반려됨"}
            </button>
          ))}
        </div>

        {/* 3. 리스트 영역 */}
        <div className="history-list">
          {filteredRequests.length === 0 ? (
            <div className="empty-history">
              <span style={{ fontSize: "40px" }}>📭</span>
              <p>해당하는 요청 내역이 없습니다.</p>
            </div>
          ) : (
            filteredRequests.map((req) => (
              <div
                key={req.id}
                className={`history-card-pro ${
                  expandedId === req.id ? "expanded" : ""
                }`}
              >
                {/* 헤더 (항상 보임) */}
                <div
                  className="card-header"
                  onClick={() => toggleExpand(req.id)}
                >
                  <div className="header-left">
                    <div className={`status-dot ${req.status}`}></div>
                    <div className="req-date">{req.date}</div>
                    <div className="req-title">{req.title}</div>
                  </div>
                  <div className="header-right">
                    <div className="req-amount">
                      {req.totalAmount.toLocaleString()}원
                    </div>
                    <div className={`status-badge ${req.status}`}>
                      {req.status === "pending"
                        ? "결재 대기"
                        : req.status === "approved"
                        ? "승인됨"
                        : "반려됨"}
                    </div>
                    <div className="arrow-icon">
                      {expandedId === req.id ? "▲" : "▼"}
                    </div>
                  </div>
                </div>

                {/* 상세 내용 (펼쳐졌을 때만 보임) */}
                {expandedId === req.id && (
                  <div className="card-detail">
                    {/* 진행 상태 바 (Stepper) */}
                    <div className="progress-stepper">
                      <div className={`step completed`}>기안 상신</div>
                      <div className="line completed"></div>
                      <div
                        className={`step ${
                          req.status !== "pending" ? "completed" : "active"
                        }`}
                      >
                        담당자 확인
                      </div>
                      <div
                        className={`line ${
                          req.status !== "pending" ? "completed" : ""
                        }`}
                      ></div>
                      <div
                        className={`step ${
                          req.status === "approved"
                            ? "completed"
                            : req.status === "rejected"
                            ? "error"
                            : ""
                        }`}
                      >
                        {req.status === "approved"
                          ? "최종 승인"
                          : req.status === "rejected"
                          ? "반려됨"
                          : "승인 대기"}
                      </div>
                    </div>

                    {/* 반려 사유 */}
                    {req.status === "rejected" && (
                      <div className="reject-alert">
                        <strong>🚨 반려 사유:</strong>{" "}
                        {req.rejectReason || "예산 초과 또는 사유 불충분"}
                      </div>
                    )}

                    {/* 품목 리스트 테이블 */}
                    <div className="item-table-wrapper">
                      <table className="item-table">
                        <thead>
                          <tr>
                            <th>품목명</th>
                            <th>수량</th>
                            <th>금액</th>
                          </tr>
                        </thead>
                        <tbody>
                          {req.items &&
                            req.items.map((item, idx) => (
                              <tr key={idx}>
                                <td>{item.name}</td>
                                <td>{item.quantity}개</td>
                                <td>
                                  {(
                                    item.price * item.quantity
                                  ).toLocaleString()}
                                  원
                                </td>
                              </tr>
                            ))}
                        </tbody>
                      </table>
                    </div>

                    <div className="memo-box">
                      <span className="label">📝 기안 메모:</span>{" "}
                      {req.memo || "없음"}
                    </div>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </AppLayout>
  );
}
