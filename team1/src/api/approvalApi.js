import axiosInstance from "./axiosInstance";

// D:\uj\fullstack 패턴: 함수로 export
export const getApprovalRequests = async (params) => {
  const res = await axiosInstance.get("/approval-requests/list", { params });
  return res.data;
};

export const getExpenseApprovals = async (params) => {
  const res = await axiosInstance.get("/approval-requests/types/expense", { params });
  return res.data;
};

export const getProductApprovals = async (params) => {
  const res = await axiosInstance.get("/approval-requests/types/product", { params });
  return res.data;
};

export const getApprovalRequest = async (id) => {
  const res = await axiosInstance.get(`/approval-requests/${id}`);
  return res.data;
};

export const getApprovalLogs = async (id) => {
  const res = await axiosInstance.get(`/approval-requests/${id}/logs`);
  return res.data;
};

export const actionApproval = async (id, data) => {
  const res = await axiosInstance.put(`/approval-requests/${id}/action`, data);
  return res.data;
};

// 기존 코드와의 호환성을 위한 객체 export (점진적 마이그레이션)
export const approvalApi = {
  getApprovalRequests: (params) => {
    return axiosInstance.get("/approval-requests/list", { params });
  },
  getExpenseApprovals: (params) => {
    return axiosInstance.get("/approval-requests/types/expense", { params });
  },
  getProductApprovals: (params) => {
    return axiosInstance.get("/approval-requests/types/product", { params });
  },
  getApprovalRequest: (id) => {
    return axiosInstance.get(`/approval-requests/${id}`);
  },
  getApprovalLogs: (id) => {
    return axiosInstance.get(`/approval-requests/${id}/logs`);
  },
  actionApproval: (id, data) => {
    return axiosInstance.put(`/approval-requests/${id}/action`, data);
  },
};

