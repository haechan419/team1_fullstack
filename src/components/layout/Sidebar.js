import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "../../styles/layout.css";

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  // ⚡ [핵심] 개발용 권한 토글 상태 (기본값: ADMIN)
  // 나중에 실제 로그인 연동 시에는 이 부분을 삭제하고 user.role 데이터를 쓰면 됩니다.
  const [userRole, setUserRole] = useState("ADMIN");

  const toggleRole = () => {
    setUserRole((prev) => (prev === "ADMIN" ? "USER" : "ADMIN"));
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">SmartSpend</div>

      {/* 🛠️ [개발 편의용] 모드 전환 버튼 (개발 끝나면 지우면 됨) */}
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
        {/* 1. [공통] 누구나 보는 메뉴 */}
        <div
          className={`nav-item ${location.pathname === "/" ? "active" : ""}`}
          onClick={() => navigate("/")}
        >
          <span style={{ marginRight: "10px" }}>🏠</span> Home
        </div>
        <div
          className={`nav-item ${
            location.pathname.includes("/approval") ? "active" : ""
          }`}
          onClick={() => navigate("/approval")}
        >
          <span style={{ marginRight: "10px" }}>✅</span> 전자결재
        </div>
        {/* 2. [공통] 업무 관련 */}
        <div
          className={`nav-item ${
            location.pathname === "/shop" ? "active" : ""
          }`}
          onClick={() => navigate("/shop")}
        >
          <span style={{ marginRight: "10px" }}>🛒</span> 비품구매
        </div>
        <div
          className={`nav-item ${
            location.pathname.includes("/tasks") ? "active" : ""
          }`}
          onClick={() => navigate("/tasks")}
        >
          <span style={{ marginRight: "10px" }}>📁</span> 업무보드
        </div>
        <div
          className={`nav-item ${
            location.pathname.includes("/mypage") ? "active" : ""
          }`}
          onClick={() => navigate("/mypage")}
        >
          <span style={{ marginRight: "10px" }}>👤</span> 마이페이지
        </div>

        {/* // [관리자 전용] 섹션*/}
        {userRole === "ADMIN" && (
          <>
            <div className="nav-label">Admin Settings</div>

            {/* 성건우 */}
            <div
              className={`nav-item ${
                location.pathname.includes("/admin/hr") ? "active" : ""
              }`}
              onClick={() => navigate("/admin/hr")}
            >
              <span style={{ marginRight: "10px" }}>👥</span> 사원관리
            </div>

            {/* 강진수 (추가) */}
            <div
              className={`nav-item ${
                location.pathname.includes("/admin/approval") ? "active" : ""
              }`}
              onClick={() => navigate("/admin/approval")}
            >
              <span style={{ marginRight: "10px" }}>📑</span> 결재관리
            </div>

            {/* 한해찬 */}
            <div
              className={`nav-item ${
                location.pathname.includes("/admin/shop") ? "active" : ""
              }`}
              onClick={() => navigate("/admin/shop")}
            >
              <span style={{ marginRight: "10px" }}>📦</span> 비품재고
            </div>

            {/* 전유진 */}
            <div
              className={`nav-item ${
                location.pathname.includes("/admin/accounting") ? "active" : ""
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
