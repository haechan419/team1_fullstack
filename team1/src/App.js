import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Provider } from "react-redux";
import { store } from "./store";
import Layout from "./components/layout/Layout";
import DashboardPage from "./pages/DashboardPage";
import ReadyPage from "./pages/ReadyPage";
// 우리가 맡은 기능
import FinanceListPage from "./pages/finance/FinanceListPage";
import ExpenseDetailPage from "./pages/finance/ExpenseDetailPage";
import ExpenseAddPage from "./pages/finance/ExpenseAddPage";
import ReceiptDetailPage from "./pages/finance/ReceiptDetailPage";
import AdminExpenseApprovalPage from "./pages/admin/approval/AdminExpenseApprovalPage";
import AdminExpenseApprovalDetailPage from "./pages/admin/approval/AdminExpenseApprovalDetailPage";
import AdminAccountingPage from "./pages/admin/accounting/AdminAccountingPage";
import "./App.css";

function App() {
  return (
    <Provider store={store}>
      <Router>
        <Layout>
          <Routes>
            {/* =================================
                1. [PUBLIC & USER] 일반 사용자용
               ================================= */}
            <Route path="/" element={<DashboardPage />} />

            {/* 강진수: 내 결재함 */}
            <Route
              path="/approval"
              element={<ReadyPage title="내 결재함/작성" />}
            />

            {/* 한해찬: 쇼핑몰 & 장바구니 */}
            <Route path="/shop" element={<ReadyPage title="비품 쇼핑몰" />} />
            <Route path="/cart" element={<ReadyPage title="장바구니" />} />

            {/* 성건우: 마이페이지 */}
            <Route path="/mypage" element={<ReadyPage title="마이페이지" />} />

            {/* 전유진: 내 지출 내역 (맡은 기능) */}
            <Route path="/expenses" element={<FinanceListPage />} />
            <Route path="/expenses/new" element={<ExpenseAddPage />} />
            <Route path="/expenses/:id" element={<ExpenseDetailPage />} />
            <Route
              path="/receipt/receipts/:id"
              element={<ReceiptDetailPage />}
            />

            {/* 문주연: 내 업무 */}
            <Route path="/tasks" element={<ReadyPage title="내 업무 보드" />} />

            {/* =================================
                2. [ADMIN] 관리자 전용
               ================================= */}
            {/* 성건우: 사원 관리 */}
            <Route
              path="/admin/hr"
              element={<ReadyPage title="[관리자] 사원 관리" />}
            />

            {/* 전유진: 통합 결재 관리 (맡은 기능)) */}
            <Route
              path="/admin/approval"
              element={<AdminExpenseApprovalPage />}
            />
            <Route
              path="/admin/approval/:id"
              element={<AdminExpenseApprovalDetailPage />}
            />

            {/* 한해찬: 비품 재고/상품 관리 */}
            <Route
              path="/admin/shop"
              element={<ReadyPage title="[관리자] 상품/재고 관리" />}
            />

            {/* 전유진: 회계 통계 (맡은 기능) */}
            <Route path="/admin/accounting" element={<AdminAccountingPage />} />

            {/* 문주연: 전체 업무 모니터링 */}
            <Route
              path="/admin/tasks"
              element={<ReadyPage title="[관리자] 팀 업무 현황" />}
            />

            {/* 기존 경로 호환성 유지 */}
            <Route path="/receipt/expenses" element={<FinanceListPage />} />
            <Route path="/receipt/expenses/new" element={<ExpenseAddPage />} />
            <Route
              path="/receipt/expenses/:id/edit"
              element={<ExpenseAddPage />}
            />
            <Route
              path="/receipt/expenses/:id"
              element={<ExpenseDetailPage />}
            />
            <Route
              path="/admin/approvals/expense"
              element={<AdminExpenseApprovalPage />}
            />
            <Route
              path="/admin/approvals/expense/:id"
              element={<AdminExpenseApprovalDetailPage />}
            />
          </Routes>
        </Layout>
      </Router>
    </Provider>
  );
}

export default App;
