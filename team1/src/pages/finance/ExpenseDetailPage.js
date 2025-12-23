import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { fetchExpense, submitExpense, deleteExpense } from "../../slices/expenseSlice";
import { getApprovalLogs } from "../../api/approvalApi";
import ReceiptUpload from "../../components/finance/ReceiptUpload";
import axiosInstance from "../../api/axiosInstance";
import "./ExpenseDetailPage.css";

const ExpenseDetailPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const { currentExpense, loading } = useSelector((state) => state.expense);
  const [approvalLogs, setApprovalLogs] = useState([]);

  useEffect(() => {
    if (id) {
      dispatch(fetchExpense(parseInt(id)));
    }
  }, [dispatch, id]);

  useEffect(() => {
    if (id && currentExpense) {
      loadApprovalLogs();
    }
  }, [id, currentExpense]);

  const loadApprovalLogs = async () => {
    // DRAFT 상태는 ApprovalRequest가 없으므로 결재 이력이 없음
    if (currentExpense.status === "DRAFT") {
      setApprovalLogs([]);
      return;
    }

    try {
      // Expense ID로 ApprovalRequest 찾기 (list API에서 필터링)
      const res = await axiosInstance.get(`/approval-requests/list`, {
        params: { requestType: "EXPENSE", size: 100 }
      });
      
      if (res.data && res.data.dtoList) {
        const approvalRequest = res.data.dtoList.find(
          (ar) => ar.refId === parseInt(id)
        );
        
        // ApprovalRequest가 있고 id가 유효한 경우에만 로그 조회
        if (approvalRequest && approvalRequest.id) {
          const logs = await getApprovalLogs(approvalRequest.id);
          setApprovalLogs(logs || []);
        } else {
          setApprovalLogs([]);
        }
      }
    } catch (error) {
      console.error("결재 이력 로드 실패:", error);
      setApprovalLogs([]);
    }
  };

  const handleSubmit = () => {
    if (id) {
      dispatch(submitExpense({ id: parseInt(id) }));
      // URL 쿼리 파라미터를 유지하여 목록 페이지로 이동 (mall 패턴)
      const queryString = searchParams.toString();
      navigate(`/receipt/expenses${queryString ? `?${queryString}` : ""}`);
    }
  };

  const handleDelete = () => {
    if (id && window.confirm("정말 삭제하시겠습니까?")) {
      dispatch(deleteExpense(parseInt(id)));
      // URL 쿼리 파라미터를 유지하여 목록 페이지로 이동 (mall 패턴)
      const queryString = searchParams.toString();
      navigate(`/receipt/expenses${queryString ? `?${queryString}` : ""}`);
    }
  };

  const handleUploadSuccess = () => {
    if (id) {
      dispatch(fetchExpense(parseInt(id)));
    }
  };

  // 보완 요청 사유 찾기
  const getRequestMoreInfoReason = () => {
    const requestMoreInfoLog = approvalLogs.find(
      (log) => log.action === "REQUEST_MORE_INFO"
    );
    return requestMoreInfoLog?.message || null;
  };

  if (loading) {
    return (
      <div className="expense-detail-page">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!currentExpense) {
    return (
      <div className="expense-detail-page">
        <div className="card">
          <p>지출 내역을 찾을 수 없습니다.</p>
        </div>
      </div>
    );
  }

  const getStatusLabel = (status) => {
    const statusMap = {
      DRAFT: "임시저장",
      SUBMITTED: "상신",
      APPROVED: "결재완료",
      REJECTED: "반려",
      REQUEST_MORE_INFO: "보완요청",
    };
    return statusMap[status || ""] || status;
  };

  return (
    <div className="expense-detail-page">
      <div className="page-header-with-tab">
        <div className="page-title-section">
          <h1 className="page-title">지출 내역 상세</h1>
          <button className="close-tab-btn" onClick={() => {
            // URL 쿼리 파라미터를 유지하여 목록 페이지로 이동 (mall 패턴)
            const queryString = searchParams.toString();
            navigate(`/receipt/expenses${queryString ? `?${queryString}` : ""}`);
          }}>
            ×
          </button>
        </div>
      </div>

      <div className="detail-actions-bar">
        {currentExpense.status === "DRAFT" && (
          <>
            <button className="btn btn-outline" onClick={() => navigate(`/receipt/expenses/${id}/edit`)}>
              수정
            </button>
            <button className="btn btn-danger" onClick={handleDelete}>
              삭제
            </button>
            <button className="btn btn-primary" onClick={handleSubmit}>
              제출
            </button>
          </>
        )}
        <button className="btn btn-secondary" onClick={() => {
          // URL 쿼리 파라미터를 유지하여 목록 페이지로 이동 (mall 패턴)
          const queryString = searchParams.toString();
          navigate(`/receipt/expenses${queryString ? `?${queryString}` : ""}`);
        }}>
          목록
        </button>
      </div>

      <div className="detail-card">
        <div className="detail-grid">
          <div className="detail-item">
            <label>전자결재 상태</label>
            <span className={`status-badge status-${currentExpense.status?.toLowerCase().replace("_", "-") || ""}`}>
              {getStatusLabel(currentExpense.status)}
            </span>
          </div>
          <div className="detail-item">
            <label>지출 일자</label>
            <span>{currentExpense.receiptDate || "-"}</span>
          </div>
          <div className="detail-item">
            <label>가맹점명</label>
            <span>{currentExpense.merchant || "-"}</span>
          </div>
          <div className="detail-item">
            <label>이용금액</label>
            <span className="amount-value">
              {currentExpense.amount ? currentExpense.amount.toLocaleString() + "원" : "-"}
            </span>
          </div>
          <div className="detail-item">
            <label>사용용도</label>
            <span>{currentExpense.category || "-"}</span>
          </div>
          <div className="detail-item">
            <label>상세내용</label>
            <span>{currentExpense.description || "-"}</span>
          </div>
          <div className="detail-item">
            <label>전자결재 상신일</label>
            <span>{currentExpense.createdAt ? currentExpense.createdAt.split("T")[0] : "-"}</span>
          </div>
          <div className="detail-item">
            <label>전자결재 승인일</label>
            <span>
              {currentExpense.status === "APPROVED" && currentExpense.updatedAt
                ? currentExpense.updatedAt.split("T")[0]
                : "-"}
            </span>
          </div>
          {currentExpense.description && (
            <div className="detail-item full-width">
              <label>메모</label>
              <span>{currentExpense.description}</span>
            </div>
          )}
        </div>
      </div>

      {/* 보완 요청 사유 표시 */}
      {currentExpense.status === "REQUEST_MORE_INFO" && getRequestMoreInfoReason() && (
        <div className="card request-more-info-card">
          <h2 className="card-title">📋 보완 요청 사유</h2>
          <div className="request-reason">
            <p>{getRequestMoreInfoReason()}</p>
          </div>
        </div>
      )}

      {/* 보완 요청 상태일 때 추가 자료 첨부 (비활성화) */}
      {currentExpense.status === "REQUEST_MORE_INFO" && (
        <div className="card">
          <h2 className="card-title">📎 추가 자료 첨부</h2>
          <div className="supplement-upload-section">
            <div className="form-group">
              <label className="form-label">보완 자료 설명 (선택사항)</label>
              <textarea
                className="form-textarea"
                disabled={true}
                placeholder="추가 자료에 대한 설명을 입력하세요 (준비 중)"
                rows={3}
                style={{ opacity: 0.6, cursor: "not-allowed" }}
              />
            </div>
            <div className="form-group">
              <label className="form-label">파일 선택</label>
              <input
                type="file"
                multiple
                accept="image/*,.pdf,.doc,.docx,.xls,.xlsx"
                disabled={true}
                className="file-input"
                style={{ opacity: 0.6, cursor: "not-allowed" }}
              />
              <div className="form-hint" style={{ color: "#9ca3af" }}>
                추가 자료 첨부 기능은 준비 중입니다
              </div>
            </div>
            <div className="supplement-actions">
              <button
                className="btn btn-primary"
                disabled={true}
                style={{ opacity: 0.6, cursor: "not-allowed" }}
                title="재제출 기능은 준비 중입니다"
              >
                재제출
              </button>
            </div>
          </div>
        </div>
      )}

      {currentExpense.status === "DRAFT" && (
        <div className="card">
          <h2 className="card-title">영수증 업로드</h2>
          {currentExpense.hasReceipt ? (
            <div>
              <p>영수증이 업로드되어 있습니다.</p>
              <button
                className="btn btn-primary"
                onClick={() => navigate(`/receipt/receipts/${currentExpense.receiptId}`)}
              >
                영수증 보기
              </button>
            </div>
          ) : (
            <ReceiptUpload
              expenseId={currentExpense.id}
              onUploadSuccess={handleUploadSuccess}
              onUploadError={(error) => alert(error)}
            />
          )}
        </div>
      )}
    </div>
  );
};

export default ExpenseDetailPage;
