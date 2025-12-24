import axiosInstance from "./axiosInstance";

// mall 패턴: 함수로 export
export const getReceipts = async (params) => {
  const res = await axiosInstance.get("/admin/receipts/list", { params });
  return res.data;
};

export const getReceipt = async (id) => {
  const res = await axiosInstance.get(`/admin/receipts/${id}`);
  return res.data;
};

export const getReceiptImage = async (id) => {
  const res = await axiosInstance.get(`/admin/receipts/${id}/image`, {
    responseType: "blob",
  });
  return res;
};

export const verifyReceipt = async (id, data) => {
  // 영수증이 있는 경우: id 사용
  if (id) {
    const res = await axiosInstance.put(`/admin/receipts/${id}/verify`, data);
    return res.data;
  } else {
    // 영수증이 없는 경우: expenseId 사용
    const res = await axiosInstance.put(`/admin/receipts/expense/${data.expenseId}/verify`, data);
    return res.data;
  }
};

export const getReceiptExtraction = async (id) => {
  const res = await axiosInstance.get(`/admin/receipts/${id}/extraction`);
  return res.data;
};

// 기존 코드와의 호환성을 위한 객체 export (점진적 마이그레이션)
export const adminReceiptApi = {
  getReceipts: (params) => {
    return axiosInstance.get("/admin/receipts/list", { params });
  },
  getReceipt: (id) => {
    return axiosInstance.get(`/admin/receipts/${id}`);
  },
  getReceiptImage: (id) => {
    return axiosInstance.get(`/admin/receipts/${id}/image`, {
      responseType: "blob",
    });
  },
  verifyReceipt: (id, data) => {
    if (id) {
      return axiosInstance.put(`/admin/receipts/${id}/verify`, data);
    } else {
      return axiosInstance.put(`/admin/receipts/expense/${data.expenseId}/verify`, data);
    }
  },
  getReceiptExtraction: (id) => {
    return axiosInstance.get(`/admin/receipts/${id}/extraction`);
  },
};

