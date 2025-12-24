import React, {useState} from "react";
import StatisticsDashboard from "./StatisticsDashboard";
import BudgetReportCenter from "./BudgetReportCenter";
import "./AdminAccountingPage.css";
import AppLayout from "../../../components/layout/AppLayout";

const AdminAccountingPage = () => {
    const [activeTab, setActiveTab] = useState("statistics"); // "statistics" | "reports"

    return (
        <AppLayout>
            <div className="admin-accounting-page">
                <div className="page-header">
                    <h1 className="page-title">회계 통계</h1>
                    <p className="page-description">
                        예산 집행 현황을 확인하고 리포트를 생성할 수 있습니다.
                    </p>
                </div>

                {/* 탭 메뉴 */}
                <div className="tab-menu">
                    <button
                        className={`tab-button ${activeTab === "statistics" ? "active" : ""}`}
                        onClick={() => setActiveTab("statistics")}
                    >
                        📊 통계 대시보드
                    </button>
                    <button
                        className={`tab-button ${activeTab === "reports" ? "active" : ""}`}
                        onClick={() => setActiveTab("reports")}
                    >
                        📄 예산 리포트
                    </button>
                </div>

                {/* 탭 컨텐츠 */}
                <div className="tab-content">
                    {activeTab === "statistics" && <StatisticsDashboard/>}
                    {activeTab === "reports" && <BudgetReportCenter/>}
                </div>
            </div>
        </AppLayout>
    );
};

export default AdminAccountingPage;

