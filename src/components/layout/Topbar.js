import React from "react";
import { useNavigate } from "react-router-dom"; // 1. 이동 기능 가져오기
import "../../styles/layout.css";

export default function Topbar() {
  const navigate = useNavigate(); // 2. 이동 함수 장착

  return (
    <header className="topbar">
      {/* 왼쪽: 리포트 버튼 */}
      <div className="topbar-left">
        <button
          className="logout-btn"
          style={{ padding: "8px 24px", fontSize: "15px" }}
          onClick={() => navigate("/report")} /* 3. 클릭하면 /report 로 이동! */
        >
          Report
        </button>
      </div>

      {/* 오른쪽: 유저 정보 및 아이콘 (기존 코드 100% 유지) */}
      <div className="topbar-right">
        <div className="user-profile">
          <div className="avatar-circle"></div>
          <div className="user-info">
            <div className="user-name">홍길동님</div>
            <div className="user-dept">0000부서</div>
          </div>
        </div>

        {/* 로그아웃 버튼 (나중에 기능 연결 필요) */}
        <button
          className="logout-btn"
          onClick={() => alert("로그아웃 기능 준비중")}
        >
          로그아웃
        </button>

        <button className="icon-btn">⚙️</button>
        <button className="icon-btn">🔔</button>
      </div>
    </header>
  );
}
