import axiosInstance from "./axiosInstance";

// D:\uj\fullstack 패턴: 함수로 export
export const uploadReceipt = async (expenseId, file) => {
  const formData = new FormData();
  formData.append("expenseId", expenseId.toString());
  formData.append("file", file);
  const res = await axiosInstance.post("/receipt/receipts/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return res.data;
};

export const getReceipt = async (id) => {
  const res = await axiosInstance.get(`/receipt/receipts/${id}`);
  return res.data;
};

export const getReceiptImage = async (id) => {
  const res = await axiosInstance.get(`/receipt/receipts/${id}/image`, {
    responseType: "blob",
  });
  return res;
};

export const getExtraction = async (id) => {
  const res = await axiosInstance.get(`/receipt/receipts/${id}/extraction`);
  return res.data;
};

export const deleteReceipt = async (id) => {
  const res = await axiosInstance.delete(`/receipt/receipts/${id}`);
  return res.data;
};

// 기존 코드와의 호환성을 위한 객체 export (점진적 마이그레이션)
export const receiptApi = {
  uploadReceipt,
  getReceipt,
  getReceiptImage,
  getExtraction,
  deleteReceipt,
};

