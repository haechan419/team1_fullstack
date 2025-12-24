import React from "react";
import {BrowserRouter, Routes, Route} from "react-router-dom";
import DashboardPage from "./pages/dashboard/DashboardPage";
import ReadyPage from "./pages/ReadyPage";
import ShopPage from "./pages/shop/ShopPage";
import FloatingUI from "./components/common/FloatingUI";
import LoginPage from "./pages/login/LoginPage"
import FinanceListPage from "./pages/finance/FinanceListPage";
import ExpenseDetailPage from "./pages/finance/ExpenseDetailPage";
import ExpenseAddPage from "./pages/finance/ExpenseAddPage";
import ReceiptDetailPage from "./pages/finance/ReceiptDetailPage";

//리포트 페이지 추가
import ReportAnalyticsPage1  from "./pages/report/ReportAnalyticsPage"

import AdminExpenseApprovalPage from "./pages/admin/approval/AdminExpenseApprovalPage";
import AdminExpenseApprovalDetailPage from "./pages/admin/approval/AdminExpenseApprovalDetailPage";
import AdminAccountingPage from "./pages/admin/accounting/AdminAccountingPage";

//내 결재함 페이지
import RequestHistoryPage from "./pages/history/RequestHistoryPage";

// 장바구니 관련 import
import {CartProvider} from "./context/CartContext";
import {Provider} from "react-redux";
import { store } from "./store";

function App() {
    return (
        <Provider store={store}>
            <CartProvider>
                <BrowserRouter>
                    {/* 장바구니 패널은 페이지(Routes)와 상관없이 언제든 열려야 함*/}

                    <Routes>
                        {/*진입 시*/}
                        <Route path="/" element={<LoginPage/>}/>
                        {/* 일반 사용자용 */}
                        <Route path="/dashboard" element={<DashboardPage/>}/>


                        {/* 강진수 : 내 결재함 */}
                        <Route
                            path="/approval"
                            element={<ReadyPage title="내 결재함/작성"/>}
                        />

                        {/* 한해찬: 쇼핑몰 */}
                        <Route path="/shop" element={<ShopPage/>}/>

                        {/* 강진수 : 장바구니 (페이지 버전 - 필요하다면 유지) */}
                        <Route path="/cart" element={<ReadyPage title="내 지출 내역"/>}/>

                        {/* 성건우 : 마이페이지 */}
                        <Route path="/mypage" element={<ReadyPage title="마이페이지"/>}/>

                        {/* 전유진 : 내 지출 내역 */}
                        <Route path="/expenses" element={<FinanceListPage/>}/>
                        <Route path="/expenses/new" element={<ExpenseAddPage/>}/>
                        <Route path="/expenses/:id" element={<ExpenseDetailPage/>}/>
                        <Route
                            path="/receipt/receipts/:id"
                            element={<ReceiptDetailPage/>}
                        />

                        {/* 문주연 : 내 업무 */}
                        <Route path="/tasks" element={<ReportAnalyticsPage1 />} />


                        {/* ---------------------------------------- */}
                        {/* 관리자 전용 (URL: /admin/...) */}

                        {/* 성건우: 사원 관리 */}
                        <Route
                            path="/admin/hr"
                            element={<ReadyPage title="[관리자] 사원 관리"/>}
                        />

                        {/* 전유진 : 통합 결재 관리 */}
                        <Route
                            path="/admin/approval"
                            element={<AdminExpenseApprovalPage/>}
                        />
                        <Route
                            path="/admin/approval/:id"
                            element={<AdminExpenseApprovalDetailPage/>}
                        />

                        {/* 한해찬 : 비품 재고 / 상품 관리 */}
                        <Route
                            path="/admin/shop"
                            element={<ReadyPage title="[관리자] 상품/재고 관리"/>}
                        />

                        {/* 전유진 : 회계 통계 */}
                        <Route path="/admin/accounting" element={<AdminAccountingPage/>}/>

                        {/* 문주연 : 전체 업무 모니터링 */}
                        <Route
                            path="/admin/tasks"
                            element={<ReadyPage title="[관리자] 팀 업무 현황"/>}
                        />

                        {/* 👇 [NEW] 내 결재함 (주문 내역 확인) */}
                        <Route path="/history" element={<RequestHistoryPage/>}/>

                        {/* 기존 경로 호환성 유지 */}
                        <Route path="/receipt/expenses" element={<FinanceListPage/>}/>
                        <Route path="/receipt/expenses/new" element={<ExpenseAddPage/>}/>
                        <Route
                            path="/receipt/expenses/:id/edit"
                            element={<ExpenseAddPage/>}
                        />
                        <Route
                            path="/receipt/expenses/:id"
                            element={<ExpenseDetailPage/>}
                        />
                        <Route
                            path="/admin/approvals/expense"
                            element={<AdminExpenseApprovalPage/>}
                        />
                        <Route
                            path="/admin/approvals/expense/:id"
                            element={<AdminExpenseApprovalDetailPage/>}
                        />
                    </Routes>
                </BrowserRouter>
            </CartProvider>
        </Provider>
    );
}

export default App;