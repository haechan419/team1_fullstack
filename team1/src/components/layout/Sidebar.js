import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";
import "./Sidebar.css";
import "../../styles/layout.css";

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentUser } = useSelector((state) => state.user);
  
  // 관리자 여부 확인
  const isAdmin = currentUser?.role === "ADMIN";

  const menuItems = [
    { path: "/", label: "Home", icon: "🏠", available: true },
    { path: "/approval", label: "전자결재", icon: "✅", available: true },
    { path: "/shop", label: "비품구매", icon: "🛒", available: true },
    { path: "/tasks", label: "업무보드", icon: "📁", available: true },
    { path: "/mypage", label: "마이페이지", icon: "👤", available: true },
    { path: "/expenses", label: "내 지출 내역", icon: "💰", available: true }, // 우리가 맡은 기능
    { path: "/history", label: "구매 신청 내역", icon: "📂", available: true }, // master에서 추가
  ];

  // 관리자 메뉴 (별도 처리)
  const adminMenuItems = [
    { path: "/admin/hr", label: "사원관리", icon: "👥", available: isAdmin },
    { path: "/admin/approval", label: "결재관리", icon: "📑", available: isAdmin },
    { path: "/admin/shop", label: "비품재고", icon: "📦", available: isAdmin },
    { path: "/admin/accounting", label: "회계통계", icon: "📊", available: isAdmin }, // 우리가 맡은 기능
  ];

  const isActive = (path) => {
    if (path === "/") {
      return location.pathname === "/";
    }
    return location.pathname.startsWith(path);
  };


  // available이 false인 메뉴는 필터링
  const visibleMenuItems = menuItems.filter(item => item.available !== false);
  const visibleAdminMenuItems = adminMenuItems.filter(item => item.available !== false);

  return (
    <aside className="app-sidebar sidebar">
      <div className="sidebar-logo">SmartSpend</div>
      <nav className="sidebar-nav">
        <ul className="nav-menu">
          {/* 일반 메뉴 */}
          {visibleMenuItems.map((item) => (
            <li 
              key={item.path} 
              className={`nav-item ${isActive(item.path) ? "active" : ""}`}
            >
              <button 
                className="nav-link" 
                onClick={() => navigate(item.path)}
              >
                <span className="nav-icon">{item.icon}</span>
                <span className="nav-label-text">{item.label}</span>
              </button>
            </li>
          ))}
        </ul>
        
        {/* 관리자 메뉴 섹션 */}
        {isAdmin && visibleAdminMenuItems.length > 0 && (
          <>
            <div className="nav-label">Admin Settings</div>
            <ul className="nav-menu">
              {visibleAdminMenuItems.map((item) => (
                <li 
                  key={item.path} 
                  className={`nav-item ${isActive(item.path) ? "active" : ""}`}
                >
                  <button 
                    className="nav-link" 
                    onClick={() => navigate(item.path)}
                  >
                    <span className="nav-icon">{item.icon}</span>
                    <span className="nav-label-text">{item.label}</span>
                  </button>
                </li>
              ))}
            </ul>
          </>
        )}
        
        {/* 하단 고정 - 설정 */}
        <div className="mt-auto pb-5">
          <ul className="nav-menu">
            <li className="nav-item">
              <button className="nav-link">
                <span className="nav-icon">⚙️</span>
                <span className="nav-label-text">설정</span>
              </button>
            </li>
          </ul>
        </div>
      </nav>
    </aside>
  );
};

export default Sidebar;
