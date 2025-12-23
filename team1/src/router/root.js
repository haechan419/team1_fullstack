import {createBrowserRouter} from "react-router-dom";
import {lazy, Suspense} from "react";
import adminRouter from "./adminRouter";

const Loading = <div>준비중....</div>
const Main = lazy(() => import("../pages/dashboard/DashboardPage"))
const Ready = lazy(() => import("../pages/ReadyPage"))
const Products = lazy(() => import("../pages/products/ProductsPage"));

const root = createBrowserRouter([
    // 일반 사용자용
    {
        // 강진수 : 메인 페이지
        path: "/",
        element: <Suspense fallback={Loading}><Main/></Suspense>
    },
    {
        // 강진수 : 내 결재함
        path: "/approval",
        element: <Suspense fallback={Loading}><Ready/></Suspense>
    },
    {
        // 강진수 : 쇼핑몰
        path: "/shop",
        element: <Suspense fallback={Loading}><Products/></Suspense>
    },
    {
        // 강진수 : 장바구니
        path: "/cart",
        element: <Suspense fallback={Loading}><Ready/></Suspense>
    },
    {
        // 성건우 : 마이페이지
        path: "/myPage",
        element: <Suspense fallback={Loading}><Ready/></Suspense>
    },
    {
        // 전유진 : 내 지출 내역
        path: "/expenses",
        element: <Suspense fallback={Loading}><Ready/></Suspense>
    },
    {
        // 문주연 : 내 업무
        path: "/tasks",
        element: <Suspense fallback={Loading}><Ready/></Suspense>
    },
    // 관리자 전용 (URL: /admin/...)
    {
        children: adminRouter()
    },

])

export default root;