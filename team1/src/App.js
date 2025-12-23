import React from "react";
import{BrowserRouter, Routes, Route} from "react-router-dom";
// ...다른 페이지 import
import DashboardPage from "./pages/dahboard/DashboardPage";
import ReadyPage from "./pages/ReadyPage";

export default function App() {
    // return <ReportAnalyticsPage />;
    return (
        <BrowserRouter>
            {/* 일반 사용자용 */}
            <Routes>
                <Route>
                    <Route path="/" element={<DashboardPage/>}/>
                </Route>

                {/* 강진수 : 내 결재함 */}
                <Route>
                    <Route path="/approval" element={<ReadyPage title="내 결재함/작성 "/>}/>
                </Route>

                {/* 한해찬: 쇼핑몰 */}
                <Route>
                    <Route path="/shop" element={<ReadyPage title="비품 쇼핑몰"/>}/>
                </Route>

                {/* 강진수 : 장바구니 */}
                <Route>
                    <Route path="/cart" element={<ReadyPage title="내 지출 내역"/>}/>
                </Route>

                {/* 성건우 : 마이페이지 */}
                <Route>
                    <Route path="/mypage" element={<ReadyPage title="마이페이지"/>}/>
                </Route>

                {/* 전유진 : 내 지출 내역 */}
                <Route>
                    <Route path="/expenses" element={<ReadyPage title="내 지출 내역"/>}/>
                </Route>

                {/* 문주연 : 내 업무 */}
                <Route>
                    <Route path="/tasks" element={<ReadyPage title="내 업무 보드"/>}/>
                </Route>

                {/* ---------------------------------------- */}
                {/* 관리자 전용 (URL: /admin/... */}
                {/* 성건우: 사원 관리 */}
                <Route>
                    <Route path="/admin/hr" element={<ReadyPage title="[관리자] 사원 관리"/>}/>
                </Route>

                {/* 강진수 : 통합 결재 관리 */}
                <Route>
                    <Route path="/admin/approval" element={<ReadyPage title="[관리자] 결재 문서 관리"/>}/>
                </Route>

                {/* 한해찬 : 비품 재고 / 상품 관리 */}
                <Route>
                    <Route path="/admin/shop" element={<ReadyPage title="[관리자] 상품/재고 관리"/>}/>
                </Route>

                {/* 전유진 : 회계 통계 */}
                <Route>
                    <Route path="/admin/accounting" element={<ReadyPage title="[관리자] 회계/지출 통계"/>}/>
                </Route>

                {/* 문주연 : 전체 업무 모니터링 */}
                <Route>
                    <Route path="/admin/tasks" element={<ReadyPage title="[관리자] 팀 업무 현황"/>}/>
                </Route>
                {/* ---------------------------------------- */}
            </Routes>
        </BrowserRouter>
    )
}
