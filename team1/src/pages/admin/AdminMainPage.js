import React from "react";
import { useNavigate } from "react-router-dom";
import "./AdminMainPage.css";

const AdminMainPage = () => {
  const navigate = useNavigate();

  const adminMenuItems = [
    {
      path: "/admin/receipts",
      label: "영수증 관리",
      description: "업로드된 영수증을 검토하고 승인/반려 처리를 할 수 있습니다.",
      icon: "🧾",
    },
    {
      path: "/admin/products",
      label: "비품 관리",
      description: "비품 구매 요청을 검토하고 승인/반려 처리를 할 수 있습니다.",
      icon: "📦",
    },
    {
      path: "/admin/members",
      label: "사원 관리",
      description: "사원 정보를 조회하고 관리할 수 있습니다.",
      icon: "👥",
    },
  ];

  return (
    <div className="admin-main-page">
      <div className="page-header">
        <h1 className="page-title">관리자 승인</h1>
        <p className="page-description">
          관리자 승인 페이지에서 영수증, 비품, 사원 관리를 할 수 있습니다.
        </p>
      </div>

      <div className="admin-menu-grid">
        {adminMenuItems.map((item) => (
          <div
            key={item.path}
            className="admin-menu-card"
            onClick={() => navigate(item.path)}
          >
            <div className="menu-icon">{item.icon}</div>
            <h2 className="menu-title">{item.label}</h2>
            <p className="menu-description">{item.description}</p>
            <div className="menu-arrow">→</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminMainPage;

