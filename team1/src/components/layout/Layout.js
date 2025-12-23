import React, { useEffect } from "react";
import { useDispatch } from "react-redux";
import { fetchCurrentUser } from "../../slices/userSlice";
import AppLayout from "./AppLayout";

const Layout = ({ children }) => {
  const dispatch = useDispatch();

  useEffect(() => {
    // 앱 시작 시 현재 사용자 정보 조회
    dispatch(fetchCurrentUser());
  }, [dispatch]);

  return <AppLayout>{children}</AppLayout>;
};

export default Layout;

