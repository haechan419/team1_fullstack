// src/constants/reportTypes.js
export const REPORT_TYPES = [
    // ===== EMPLOYEE =====
    {
        id: "personal_detail_excel",
        label: "Personal Detailed Records (Excel)",
        outputFormat: "Excel",
        roles: ["EMPLOYEE"],
        preview: { recordsIncluded: 42, totalAmount: "₩1,240,000" },
    },
    {
        id: "personal_summary_pdf",
        label: "Personal Summary Report (PDF)",
        outputFormat: "PDF",
        roles: ["EMPLOYEE"],
        preview: { recordsIncluded: 42, totalAmount: "₩1,240,000" },
    },

    // ===== ADMIN =====
    {
        id: "dept_detail_excel",
        label: "Department Detailed Records (Excel)",
        outputFormat: "Excel",
        roles: ["ADMIN"],
        preview: { recordsIncluded: 128, totalAmount: "₩7,920,000" },
    },
    {
        id: "dept_summary_pdf",
        label: "Department Summary Report (PDF)",
        outputFormat: "PDF",
        roles: ["ADMIN"],
        preview: { recordsIncluded: 128, totalAmount: "₩7,920,000" },
    },
    {
        id: "ai_strategy_pdf",
        label: "AI Strategy Insight Report (PDF)",
        outputFormat: "PDF",
        roles: ["ADMIN"],
        preview: { recordsIncluded: 128, totalAmount: "₩7,920,000" },
    },
];
