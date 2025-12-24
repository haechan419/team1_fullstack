import "../../styles/layout.css";
import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  // [핵심] 개발용 권한 토글 (기본값: ADMIN)
  const [userRole, setUserRole] = useState("ADMIN");

  const toggleRole = () => {
    setUserRole((prev) => (prev === "ADMIN" ? "USER" : "ADMIN"));
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">SmartSpend</div>

      {/* [개발 편의용] 모드 전환 버튼 */}
      <div style={{ padding: "10px 20px" }}>
        <button
          onClick={toggleRole}
          style={{
            width: "100%",
            padding: "8px",
            background: userRole === "ADMIN" ? "#e74c3c" : "#2ecc71",
            border: "none",
            borderRadius: "6px",
            color: "white",
            fontWeight: "bold",
            cursor: "pointer",
            fontSize: "12px",
          }}
        >
          현재: {userRole === "ADMIN" ? "관리자 모드" : "사원 모드"} 🔄
        </button>
      </div>

      <nav className="sidebar-nav">
        {/* 1. [공통] 메인 메뉴 */}
        <div
          className={`nav-item ${location.pathname === "/dashboard" ? "active" : ""}`}
          onClick={() => navigate("/dashboard")}
        >
          <span style={{ marginRight: "10px" }}>🏠</span> Home
        </div>

        <div
          className={`nav-item ${
            location.pathname === "/approval" ? "active" : ""
          }`}
          onClick={() => navigate("/approval")}
        >
          <span style={{ marginRight: "10px" }}>✅</span> 전자결재
        </div>

        {/* 👇 [수정됨] 비품 구매 그룹 (메인 + 하위 메뉴) */}
        <div className="nav-group">
          {/* 메인: 비품 구매 */}
          <div
            className={`nav-item ${
              location.pathname === "/shop" ? "active" : ""
            }`}
            onClick={() => navigate("/shop")}
          >
            <span style={{ marginRight: "10px" }}>🛒</span> 비품구매
          </div>
        </div>

          {/* 하위: 구매 신청 내역 (들여쓰기 적용) */}
          <div
            className={`nav-item ${
              location.pathname === "/history" ? "active-sub" : ""
            }`}
            onClick={() => navigate("/history")}
          >
            <span style={{ marginRight: "8px" }}>└</span> 📂 구매 신청 내역
          </div>

        {/* ------------------------------------------------ */}

        <div
          className={`nav-item ${
            location.pathname === "/tasks" ? "active" : ""
          }`}
          onClick={() => navigate("/tasks")}
        >
          <span style={{ marginRight: "10px" }}>📁</span> 업무보드
        </div>

        <div
          className={`nav-item ${
            location.pathname === "/mypage" ? "active" : ""
          }`}
          onClick={() => navigate("/mypage")}
        >
          <span style={{ marginRight: "10px" }}>👤</span> 마이페이지
        </div>

        {/* 2. [관리자 전용] 섹션 */}
        {userRole === "ADMIN" && (
          <>
            <div className="nav-label">Admin Settings</div>

            <div
              className={`nav-item ${
                location.pathname === "/admin/hr" ? "active" : ""
              }`}
              onClick={() => navigate("/admin/hr")}
            >
              <span style={{ marginRight: "10px" }}>👥</span> 사원관리
            </div>

            <div
              className={`nav-item ${
                location.pathname === "/admin/approval" ? "active" : ""
              }`}
              onClick={() => navigate("/admin/approval")}
            >
              <span style={{ marginRight: "10px" }}>📑</span> 결재관리
            </div>

            <div
              className={`nav-item ${
                location.pathname === "/admin/shop" ? "active" : ""
              }`}
              onClick={() => navigate("/admin/shop")}
            >
              <span style={{ marginRight: "10px" }}>📦</span> 비품재고
            </div>

            <div
              className={`nav-item ${
                location.pathname === "/admin/accounting" ? "active" : ""
              }`}
              onClick={() => navigate("/admin/accounting")}
            >
              <span style={{ marginRight: "10px" }}>📊</span> 회계통계
            </div>
          </>
        )}
      </nav>

      {/* 하단 고정 */}
      <div style={{ marginTop: "auto", paddingBottom: "20px" }}>
        <div className="nav-item">
          <span style={{ marginRight: "10px" }}>⚙️</span> 설정
        </div>
      </div>
    </aside>
  );
}
