import axiosInstance from "./axiosInstance";

// mallapi 패턴: 함수로 export
export const getDepartmentStatistics = async (params) => {
  const res = await axiosInstance.get("/admin/accounting/statistics/department", { params });
  return res.data;
};

export const getCategoryStatistics = async (params) => {
  const res = await axiosInstance.get("/admin/accounting/statistics/category", { params });
  return res.data;
};

export const getSummary = async () => {
  const res = await axiosInstance.get("/admin/accounting/statistics/summary");
  return res.data;
};

export const getOverBudgetList = async () => {
  const res = await axiosInstance.get("/admin/accounting/statistics/over-budget");
  return res.data;
};

export const getDepartments = async () => {
  const res = await axiosInstance.get("/admin/accounting/departments");
  return res.data;
};

export const generateReport = async (data) => {
  const res = await axiosInstance.post("/admin/accounting/reports", data);
  return res.data;
};

export const getReportList = async (params) => {
  const res = await axiosInstance.get("/admin/accounting/reports", { params });
  return res.data;
};

export const downloadReport = async (reportId) => {
  const res = await axiosInstance.get(`/admin/accounting/reports/${reportId}/download`, {
    responseType: "blob",
  });
  return res.data;
};

// 기존 코드와의 호환성을 위한 객체 export (점진적 마이그레이션)
export const accountingApi = {
  getDepartmentStatistics: (params) => {
    return axiosInstance.get("/admin/accounting/statistics/department", { params });
  },
  getCategoryStatistics: (params) => {
    return axiosInstance.get("/admin/accounting/statistics/category", { params });
  },
  getSummary: () => {
    return axiosInstance.get("/admin/accounting/statistics/summary");
  },
  getOverBudgetList: () => {
    return axiosInstance.get("/admin/accounting/statistics/over-budget");
  },
  getDepartments: () => {
    return axiosInstance.get("/admin/accounting/departments");
  },
  generateReport: (data) => {
    return axiosInstance.post("/admin/accounting/reports", data);
  },
  getReportList: (params) => {
    return axiosInstance.get("/admin/accounting/reports", { params });
  },
  downloadReport: (reportId) => {
    return axiosInstance.get(`/admin/accounting/reports/${reportId}/download`, {
      responseType: "blob",
    });
  },
};

