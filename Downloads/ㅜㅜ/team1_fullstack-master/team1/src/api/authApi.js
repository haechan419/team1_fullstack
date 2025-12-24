import axios from "axios";

// 백엔드 API 기본 URL 설정 (환경에 따라 변경 가능)
const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // CORS credentials 허용
  headers: {
    "Content-Type": "application/x-www-form-urlencoded", // Spring Security Form Login은 form-data를 기대함
  },
});

/**
 * 로그인 API
 * @param {string} employeeNo - 사번 (로그인 ID)
 * @param {string} password - 비밀번호
 * @returns {Promise} axios response
 */
export const login = async (employeeNo, password) => {
  try {
    // Spring Security Form Login은 URL-encoded 형식을 기대함
    const params = new URLSearchParams();
    params.append("employeeNo", employeeNo);
    params.append("password", password);

    const response = await apiClient.post("/api/auth/login", params);
    return response;
  } catch (error) {
    // 에러 응답 처리
    if (error.response) {
      // 서버가 응답했지만 에러 상태 코드
      throw error.response;
    } else if (error.request) {
      // 요청이 전송되었지만 응답을 받지 못함
      throw {
        data: {
          success: false,
          error: "NETWORK_ERROR",
          message: "서버에 연결할 수 없습니다.",
        },
        status: 0,
      };
    } else {
      // 요청 설정 중 에러 발생
      throw {
        data: {
          success: false,
          error: "REQUEST_ERROR",
          message: "요청 중 오류가 발생했습니다.",
        },
        status: 0,
      };
    }
  }
};

export default {
  login,
};
