import React, { useState } from "react";
import "./LoginPage.css";
import { login } from "../api/authApi";

const LoginPage = () => {
  const [formData, setFormData] = useState({
    employeeNo: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // 입력 시 에러 메시지 초기화
    if (error) setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await login(formData.employeeNo, formData.password);

      // 성공 응답 처리
      if (response.data && response.data.success) {
        console.log("로그인 성공:", response.data);
        // TODO: 나중에 Redux로 로그인 상태 관리 및 JWT 토큰 저장
        alert("로그인 성공!");
      }
    } catch (errorResponse) {
      // 에러 응답 처리
      const errorData = errorResponse.data || {};
      const errorMessage = errorData.message || "로그인에 실패했습니다.";
      setError(errorMessage);
      console.error("로그인 실패:", errorData);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="login-title">로그인</h1>
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="input-group">
            <label htmlFor="employeeNo">아이디</label>
            <input
              type="text"
              id="employeeNo"
              name="employeeNo"
              value={formData.employeeNo}
              onChange={handleChange}
              placeholder="아이디를 입력해주세요."
              required
            />
          </div>

          <div className="input-group">
            <label htmlFor="password">비밀번호</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="비밀번호를 입력해주세요"
              required
            />
          </div>

          {error && (
            <div
              className="error-message"
              style={{ color: "red", fontSize: "14px" }}
            >
              {error}
            </div>
          )}

          <button type="submit" className="signup-button" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
