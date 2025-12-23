# Agentic ERP - SmartSpend (스마트스펜드)

## 프로젝트 개요
AI 에이전트가 판단하고, RPA가 실행하는 지능형 재무 자동화 플랫폼
- **슬로건**: "AI 에이전트가 판단하고, RPA가 실행하는 지능형 재무 자동화 플랫폼"
- **핵심 컨셉**: 사원에게는 '지능형 업무 비서', 관리자에게는 '자동화된 의사결정 도구' 제공

## 프로젝트 구조

### 백엔드 (team1api/)
```
team1api/
├── src/main/java/com/Team1_Back/
│   ├── config/
│   │   └── SecurityConfig.java          # Spring Security 설정
│   ├── domain/
│   │   ├── User.java                    # 사용자 엔티티
│   │   ├── LoginAttempt.java            # 로그인 시도 기록 엔티티
│   │   └── Role.java                    # 권한 enum (USER, ADMIN)
│   ├── dto/
│   │   └── auth/
│   │       ├── LoginRequestDTO.java     # 로그인 요청 DTO
│   │       ├── LoginResponseDTO.java    # 로그인 응답 DTO
│   │       └── UserDTO.java             # Spring Security용 UserDetails 구현체
│   ├── repository/
│   │   ├── UserRepository.java          # 사용자 데이터 접근
│   │   └── LoginAttemptRepository.java  # 로그인 시도 기록 접근
│   ├── security/
│   │   ├── CustomUserDetailsService.java # 사용자 정보 로드 서비스
│   │   ├── handler/
│   │   │   ├── APILoginSuccessHandler.java  # 로그인 성공 핸들러
│   │   │   └── APILoginFailHandler.java      # 로그인 실패 핸들러
│   │   └── listener/
│   │       ├── LoginEventListener.java      # 로그인 이벤트 리스너
│   │       ├── LoginSuccessEvent.java       # 로그인 성공 이벤트
│   │       └── LoginFailureEvent.java       # 로그인 실패 이벤트
│   └── service/
│       ├── AuthService.java             # 인증 서비스 인터페이스
│       └── AuthServiceImpl.java        # 인증 서비스 구현체
└── src/main/resources/
    └── application.properties          # 설정 파일
```

## 로그인 인증 처리 흐름

### 1. 전체 인증 흐름도

```
[클라이언트 요청]
    ↓
[SecurityConfig - SecurityFilterChain]
    ↓
[Spring Security Form Login]
    ↓
[CustomUserDetailsService.loadUserByUsername()]
    ├─ 사번(employeeNo)으로 User 조회
    ├─ User → UserDTO 변환
    └─ UserDetails 반환
    ↓
[Spring Security PasswordEncoder]
    ├─ 입력 비밀번호 vs DB 암호화 비밀번호 비교
    └─ 일치 여부 확인
    ↓
[성공]                    [실패]
    ↓                        ↓
[APILoginSuccessHandler]  [APILoginFailHandler]
    ↓                        ↓
[LoginSuccessEvent 발행]   [LoginFailureEvent 발행]
    ↓                        ↓
[LoginEventListener]      [LoginEventListener]
    ├─ 실패 횟수 초기화      ├─ 실패 횟수 증가
    └─ 로그인 기록 저장       ├─ 계정 잠금 처리 (5회 이상)
                              └─ 로그인 기록 저장
    ↓                        ↓
[JSON 응답 반환]           [JSON 에러 응답 반환]
```

### 2. 사번과 비밀번호 처리 과정

#### 2.1 사번(EmployeeNo) 처리
1. **요청 수신**
   - 클라이언트가 `/api/auth/login`으로 POST 요청
   - 요청 파라미터: `employeeNo`, `password`
   - `SecurityConfig`에서 `usernameParameter("employeeNo")` 설정

2. **사번 조회**
   - `CustomUserDetailsService.loadUserByUsername(String employeeNo)` 호출
   - `UserRepository.findByEmployeeNo(employeeNo)`로 DB 조회
   - 사번이 존재하지 않으면 `UsernameNotFoundException` 발생

3. **사번 검증**
   - User 엔티티의 `employeeNo`는 `@Column(unique = true, nullable = false)` 제약 조건
   - DB 레벨에서 중복 방지

#### 2.2 비밀번호(Password) 처리
1. **비밀번호 수신**
   - 클라이언트로부터 평문 비밀번호 수신
   - `SecurityConfig`에서 `passwordParameter("password")` 설정

2. **비밀번호 인코딩**
   - `SecurityConfig`에서 `BCryptPasswordEncoder` 빈 등록
   - DB에 저장된 비밀번호는 이미 BCrypt로 암호화된 상태

3. **비밀번호 검증**
   - Spring Security가 자동으로 처리
   - `CustomUserDetailsService`에서 반환한 `UserDTO`의 `password` 필드와
   - 클라이언트가 입력한 평문 비밀번호를 `BCryptPasswordEncoder.matches()`로 비교
   - 일치하지 않으면 `AuthenticationException` 발생

4. **비밀번호 저장 규칙**
   - User 엔티티의 `password` 필드는 `@Column(length = 255)`로 설정
   - BCrypt 해시는 최대 60자이지만, 향후 확장을 위해 255자로 설정

### 3. 리스너(Listener) 관계도

#### 3.1 이벤트 기반 아키텍처
```
┌─────────────────────────────────────────────────────────┐
│                    SecurityConfig                        │
│  - ApplicationEventPublisher 주입                        │
│  - APILoginSuccessHandler(eventPublisher)               │
│  - APILoginFailHandler(eventPublisher)                  │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              APILoginSuccessHandler                      │
│  - onAuthenticationSuccess()                            │
│  - LoginSuccessEvent 발행                               │
│    eventPublisher.publishEvent(new LoginSuccessEvent()) │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              APILoginFailHandler                        │
│  - onAuthenticationFailure()                            │
│  - LoginFailureEvent 발행                               │
│    eventPublisher.publishEvent(new LoginFailureEvent())│
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              LoginEventListener                          │
│  @Component - Spring Bean으로 등록                      │
│  @EventListener - 이벤트 리스너로 동작                   │
│                                                        │
│  - handleLoginSuccess(LoginSuccessEvent)                │
│    ├─ User 조회                                        │
│    ├─ 실패 횟수 초기화 (resetFailedLoginCount)         │
│    └─ LoginAttempt 저장 (success=true)                 │
│                                                        │
│  - handleLoginFailure(LoginFailureEvent)                │
│    ├─ User 조회 (없으면 바로 기록 저장 후 종료)         │
│    ├─ 실패 횟수 증가 (increaseFailedLoginCount)        │
│    ├─ 5회 이상 시 계정 잠금 (lock)                      │
│    └─ LoginAttempt 저장 (success=false)                │
└─────────────────────────────────────────────────────────┘
```

#### 3.2 이벤트 클래스 구조
- **LoginSuccessEvent**
  - `employeeNo`: 사번
  - `ipAddress`: 클라이언트 IP 주소

- **LoginFailureEvent**
  - `employeeNo`: 사번
  - `ipAddress`: 클라이언트 IP 주소
  - `errorMessage`: 에러 메시지 (리스너에서 설정)

#### 3.3 리스너의 역할
1. **비즈니스 로직 분리**
   - Handler는 HTTP 응답 처리에만 집중
   - DB 작업은 Listener에서 처리하여 관심사 분리

2. **트랜잭션 관리**
   - `@Transactional` 어노테이션으로 DB 작업의 원자성 보장
   - 실패 횟수 증가와 계정 잠금이 함께 처리됨

3. **비동기 처리 가능**
   - Spring의 이벤트 메커니즘을 사용하여 향후 비동기 처리로 확장 가능

## 주요 컴포넌트 상세 설명

### SecurityConfig
- **역할**: Spring Security 전역 설정
- **주요 설정**:
  - CORS 설정 (모든 Origin 허용)
  - CSRF 비활성화 (API 서버이므로)
  - `/api/auth/**` 경로는 인증 없이 접근 가능
  - Form Login 설정 (사번/비밀번호 파라미터 지정)
  - BCryptPasswordEncoder 빈 등록

### CustomUserDetailsService
- **역할**: Spring Security가 사용자 정보를 로드할 때 호출
- **처리 과정**:
  1. 사번으로 User 엔티티 조회
  2. User → UserDTO 변환 (Spring Security UserDetails 구현)
  3. UserDTO 반환 (비밀번호 포함)

### APILoginSuccessHandler
- **역할**: 로그인 성공 시 처리
- **처리 과정**:
  1. Authentication에서 UserDTO 추출
  2. 클라이언트 IP 주소 추출
  3. LoginSuccessEvent 발행
  4. UserDTO의 claims를 JSON으로 변환하여 응답

### APILoginFailHandler
- **역할**: 로그인 실패 시 처리
- **처리 과정**:
  1. 요청에서 사번 추출
  2. 클라이언트 IP 주소 추출
  3. LoginFailureEvent 발행
  4. 이벤트 처리 후 에러 메시지 가져오기
  5. JSON 에러 응답 반환

### LoginEventListener
- **역할**: 로그인 이벤트를 받아 DB 작업 수행
- **성공 이벤트 처리**:
  - User 조회
  - `resetFailedLoginCount()` 호출
  - LoginAttempt 저장 (success=true)
- **실패 이벤트 처리**:
  - User 조회 (없으면 바로 기록 저장 후 종료)
  - `increaseFailedLoginCount()` 호출
  - 5회 이상이면 `lock()` 호출하여 계정 잠금
  - LoginAttempt 저장 (success=false)
  - 에러 메시지 설정

### User 엔티티
- **주요 필드**:
  - `employeeNo`: 사번 (로그인 ID, UNIQUE)
  - `password`: BCrypt 암호화된 비밀번호
  - `failedLoginCount`: 로그인 실패 횟수
  - `lockedAt`: 계정 잠금 시각
  - `isActive`: 재직 여부
- **주요 메서드**:
  - `isLocked()`: 계정 잠금 여부 확인
  - `increaseFailedLoginCount()`: 실패 횟수 증가
  - `resetFailedLoginCount()`: 실패 횟수 초기화
  - `lock()`: 계정 잠금
  - `unlock()`: 계정 잠금 해제

### LoginAttempt 엔티티
- **역할**: 모든 로그인 시도 기록 저장
- **주요 필드**:
  - `employeeNo`: 시도한 사번 (외래키 아님, 기록 보존용)
  - `ipAddress`: 접속 IP 주소
  - `success`: 성공/실패 여부
  - `createdAt`: 시도 시각

## 보안 기능

### 1. 계정 잠금 기능
- 로그인 실패 5회 초과 시 자동 계정 잠금
- `User.lockedAt` 필드에 잠금 시각 저장
- 잠금된 계정은 `UserDTO.isAccountNonLocked()`에서 false 반환

### 2. 로그인 시도 기록
- 모든 로그인 시도(성공/실패)를 `login_attempt` 테이블에 기록
- IP 주소 추적 가능
- 퇴사한 사용자의 기록도 보존 (외래키 관계 없음)

### 3. 비밀번호 암호화
- BCrypt 알고리즘 사용
- Spring Security의 `BCryptPasswordEncoder` 활용

## API 엔드포인트 (현재 구현 상태)

### 인증 관련
- `POST /api/auth/login` - 로그인 (Form Login 방식)
- `POST /api/auth/logout` - 로그아웃

### 향후 구현 예정
- `POST /api/auth/refresh` - 토큰 갱신
- `GET /api/users/me` - 마이페이지 정보 조회
- `GET /api/users/me/budget` - 예산 정보 조회
- `GET /api/admin/users` - 사원 목록 조회
- `POST /api/admin/users` - 사원 등록
- `PUT /api/admin/users/{id}` - 사원 정보 수정
- `DELETE /api/admin/users/{id}` - 사원 삭제
- `POST /api/admin/users/{id}/unlock` - 계정 잠금 해제
- `POST /api/admin/users/{id}/budget` - 예산 설정

## 데이터베이스 스키마

### users 테이블
- `id` (PK): 고유 번호
- `employee_no` (UNIQUE): 사번 (로그인 ID)
- `email`: 이메일
- `password`: BCrypt 암호화된 비밀번호
- `name`: 이름
- `department_name`: 부서명
- `role`: 권한 (USER/ADMIN)
- `is_active`: 재직 여부
- `failed_login_count`: 로그인 실패 횟수
- `locked_at`: 계정 잠금 시각
- `deleted_at`: 퇴사 처리 시각
- `created_user_at`: 생성 시각
- `updated_user_at`: 수정 시각

### login_attempt 테이블
- `id` (PK): 고유 번호
- `employee_no`: 시도한 사번 (외래키 아님)
- `ip_address`: 접속 IP 주소
- `success`: 성공 여부
- `created_at`: 시도 시각

### refresh_token 테이블 (향후 구현)
- `id` (PK): 고유 번호
- `user_id` (FK): 사용자 ID
- `token`: 리프레시 토큰
- `expires_at`: 만료 시각
- `created_at`: 생성 시각

### user_budget_monthly 테이블 (향후 구현)
- `id` (PK): 고유 번호
- `user_id` (FK): 사용자 ID
- `year_month`: 연월
- `monthly_limit`: 월 예산 한도
- `spent_amount`: 사용 금액
- `note`: 메모
- `created_at`: 생성 시각
- `updated_at`: 수정 시각

## 구현 순서
1. ✅ 로그인/로그아웃 기능 (현재 단계)
2. ⏳ 마이페이지 기능
3. ⏳ 사원 관리 기능

## 기술 스택
- **Backend**: Spring Boot, Spring Security
- **Database**: JPA/Hibernate
- **Password Encoding**: BCrypt
- **Build Tool**: Gradle

