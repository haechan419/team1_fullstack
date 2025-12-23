import React from "react";
<<<<<<< HEAD
import { BrowserRouter, Routes, Route } from "react-router-dom";
import DashboardPage from "./pages/dashboard/DashboardPage";
import ReadyPage from "./pages/ReadyPage";
import ShopPage from "./pages/shop/ShopPage";
import FloatingUI from "./components/common/FloatingUI";

//내 결재함 페이지
import RequestHistoryPage from "./pages/history/RequestHistoryPage";

// 장바구니 관련 import
import { CartProvider } from "./context/CartContext";

export default function App() {
  return (
    <CartProvider>
      <BrowserRouter>
        {/* 장바구니 패널은 페이지(Routes)와 상관없이 언제든 열려야 함*/}

        <Routes>
          {/* 일반 사용자용 */}
          <Route path="/" element={<DashboardPage />} />

          {/* 강진수 : 내 결재함 */}
          <Route
            path="/approval"
            element={<ReadyPage title="내 결재함/작성" />}
          />

          {/* 한해찬: 쇼핑몰 */}
          <Route path="/shop" element={<ShopPage />} />

          {/* 강진수 : 장바구니 (페이지 버전 - 필요하다면 유지) */}
          <Route path="/cart" element={<ReadyPage title="내 지출 내역" />} />

          {/* 성건우 : 마이페이지 */}
          <Route path="/mypage" element={<ReadyPage title="마이페이지" />} />

          {/* 전유진 : 내 지출 내역 */}
          <Route
            path="/expenses"
            element={<ReadyPage title="내 지출 내역" />}
          />

          {/* 문주연 : 내 업무 */}
          <Route path="/tasks" element={<ReadyPage title="내 업무 보드" />} />

          {/* ---------------------------------------- */}
          {/* 관리자 전용 (URL: /admin/...) */}

          {/* 성건우: 사원 관리 */}
          <Route
            path="/admin/hr"
            element={<ReadyPage title="[관리자] 사원 관리" />}
          />

          {/* 강진수 : 통합 결재 관리 */}
          <Route
            path="/admin/approval"
            element={<ReadyPage title="[관리자] 결재 문서 관리" />}
          />

          {/* 한해찬 : 비품 재고 / 상품 관리 */}
          <Route
            path="/admin/shop"
            element={<ReadyPage title="[관리자] 상품/재고 관리" />}
          />

          {/* 전유진 : 회계 통계 */}
          <Route
            path="/admin/accounting"
            element={<ReadyPage title="[관리자] 회계/지출 통계" />}
          />

          {/* 문주연 : 전체 업무 모니터링 */}
          <Route
            path="/admin/tasks"
            element={<ReadyPage title="[관리자] 팀 업무 현황" />}
          />

          {/* 👇 [NEW] 내 결재함 (주문 내역 확인) */}
          <Route path="/history" element={<RequestHistoryPage />} />
        </Routes>
      </BrowserRouter>
    </CartProvider>
  );
=======
import { RouterProvider } from 'react-router-dom';
import root from './router/root';
import './App.css';
function App() {
    return (
        <RouterProvider router={root}/>
    );
>>>>>>> 97316a89ce41d9f321441590caf55a2d10333061
}

export default App;