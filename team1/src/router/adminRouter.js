import {lazy, Suspense} from "react";
const Loading = <div>준비중....</div>
const Ready = lazy(() => import("../pages/ReadyPage"))

const adminRouter = () => {
    return [
        {
            // 성건우 : 사원 관리
            path: "/admin/hr",
            element: <Suspense fallback={Loading}><Ready/></Suspense>
        },
        {
            // 강진수 : 통합 결재 관리
            path: "/admin/approval",
            element: <Suspense fallback={Loading}><Ready/></Suspense>
        },
        {
            // 강진수 : 비품 재고 / 상품 관리
            path: "/admin/shop",
            element: <Suspense fallback={Loading}><Ready/></Suspense>
        },
        {
            // 전유진 : 회계 통계
            path: "/admin/accounting",
            element: <Suspense fallback={Loading}><Ready/></Suspense>
        },
        {
            // 문주연 : 전체 업무 모니터링
            path: "/admin/tasks",
            element: <Suspense fallback={Loading}><Ready/></Suspense>
        },
    ]
}

export default adminRouter;