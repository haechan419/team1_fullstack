import axiosInstance from "./axiosInstance";

// D:\uj\fullstack 패턴: 함수로 export
export const getCurrentUser = async () => {
  const res = await axiosInstance.get("/users/me");
  return res.data;
};

// 기존 코드와의 호환성을 위한 객체 export (점진적 마이그레이션)
export const userApi = {
  getCurrentUser: () => {
    return axiosInstance.get("/users/me");
  },
};

