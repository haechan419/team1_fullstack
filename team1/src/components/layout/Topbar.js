import React from "react";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { clearUser } from "../../slices/userSlice";
import { useDispatch } from "react-redux";
import "../../styles/layout.css";

export default function Topbar() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { currentUser } = useSelector((state) => state.user);

  const handleLogout = () => {
    // 로그아웃 처리 (나중에 실제 로그인 기능 구현 시 수정 필요)
    localStorage.removeItem("token");
    dispatch(clearUser());
    navigate("/login");
  };

  return (
    <header className="topbar">
      {/* 왼쪽: 리포트 버튼 (선택사항) */}
      <div className="topbar-left">
        {/* 필요시 추가 */}
      </div>

      {/* 오른쪽: 유저 정보 및 아이콘 */}
      <div className="topbar-right">
        <div className="user-profile">
          <div className="avatar-circle"></div>
          <div className="user-info">
            <div className="user-name">{currentUser?.name || "홍길동"}님</div>
            <div className="user-dept">{currentUser?.departmentName || ""}</div>
          </div>
        </div>

        {/* 로그아웃 버튼 */}
        <button className="logout-btn" onClick={handleLogout}>
          로그아웃
        </button>

        <button className="icon-btn" title="설정">⚙️</button>
        <button className="icon-btn" title="알림">🔔</button>
      </div>
    </header>
  );
}

