import React from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/layout.css";
// import { useCart } from "../../context/CartContext"; // 👈 이제 안 쓰니까 삭제해도 됨

export default function Topbar() {
  const navigate = useNavigate();
  // const { toggleDrawer, cartItems } = useCart(); // 👈 이것도 삭제

  return (
    <header className="topbar">
      <div className="topbar-left">
        <button
          className="logout-btn"
          style={{ padding: "8px 24px", fontSize: "15px" }}
          onClick={() => navigate("/report")}
        >
          Report
        </button>
      </div>

      <div className="topbar-right">
        <div className="user-profile">
          <div className="avatar-circle"></div>
          <div className="user-info">
            <div className="user-name">홍길동님</div>
            <div className="user-dept">0000부서</div>
          </div>
        </div>

        <button
          className="logout-btn"
          onClick={() => alert("로그아웃 기능 준비중")}
        >
          로그아웃
        </button>

        {/* 🗑️ [삭제됨] 장바구니 아이콘 버튼 제거 완료 */}

        <button className="icon-btn">⚙️</button>
        <button className="icon-btn">🔔</button>
      </div>
    </header>
  );
}
