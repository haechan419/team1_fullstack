-- SmartSpend 지출 정산 센터 테스트 데이터
-- 개발/테스트 환경에서만 사용하세요!

USE smartspenddb;

-- 테스트용 사용자 데이터 (비밀번호는 모두 "password123"의 BCrypt 해시)
-- 실제 사용 시 Spring Security의 BCryptPasswordEncoder로 생성한 해시값 사용
INSERT INTO `user` (employee_no, email, password, name, department_name, role, is_active, created_at, updated_at)
VALUES 
  ('ADMIN001', 'admin@smartspend.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '관리자', '관리팀', 'ADMIN', TRUE, NOW(), NOW()),
  ('USER001', 'user1@smartspend.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '홍길동', '개발팀', 'USER', TRUE, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
  ('USER002', 'user2@smartspend.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '김철수', '영업팀', 'USER', TRUE, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
  ('USER003', 'user3@smartspend.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '이영희', '마케팅팀', 'USER', TRUE, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
  ('USER004', 'user4@smartspend.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '박민수', '개발팀', 'USER', TRUE, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 테스트용 지출 내역 데이터 (다양한 상태와 날짜)
-- 현실적인 상태 분포: SUBMITTED > APPROVED > REJECTED > REQUEST_MORE_INFO > DRAFT
INSERT INTO `expense` (user_id, approval_status, merchant, amount, category, receipt_date, description, created_at, updated_at)
VALUES
  -- USER001 (홍길동) - 개발팀
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'SUBMITTED', '스타벅스 강남점', 12000, '식비', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '팀 회의 커피', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '이마트', 31455, '비품', DATE_SUB(CURDATE(), INTERVAL 10 DAY), '사무용품 구매', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'REJECTED', '맥도날드 역삼점', 9000, '식비', DATE_SUB(CURDATE(), INTERVAL 25 DAY), '개인 식사', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'SUBMITTED', 'GS25 편의점', 5500, '기타', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '간식 구매', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'DRAFT', '교보문고', 25000, '비품', CURDATE(), '도서 구매', NOW(), NOW()),
  
  -- USER002 (김철수) - 영업팀
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '스타벅스 강남점', 15000, '식비', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '고객 미팅', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'SUBMITTED', '이마트', 45000, '비품', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '프레젠테이션 용품', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'REQUEST_MORE_INFO', '맥도날드 역삼점', 8000, '식비', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', 'GS25 편의점', 3000, '기타', DATE_SUB(CURDATE(), INTERVAL 12 DAY), '음료 구매', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'SUBMITTED', '교보문고', 18000, '비품', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '업무 서적', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
  
  -- USER003 (이영희) - 마케팅팀
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '스타벅스 강남점', 20000, '식비', DATE_SUB(CURDATE(), INTERVAL 8 DAY), '팀 회의', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'REJECTED', '이마트', 50000, '비품', DATE_SUB(CURDATE(), INTERVAL 20 DAY), '마케팅 용품', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'SUBMITTED', '맥도날드 역삼점', 11000, '식비', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', 'GS25 편의점', 7000, '기타', DATE_SUB(CURDATE(), INTERVAL 15 DAY), '간식 구매', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'DRAFT', '교보문고', 32000, '비품', CURDATE(), '마케팅 도서', NOW(), NOW()),
  
  -- USER004 (박민수) - 개발팀
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'SUBMITTED', '스타벅스 강남점', 13000, '식비', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '코드 리뷰 미팅', DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '이마트', 28000, '비품', DATE_SUB(CURDATE(), INTERVAL 11 DAY), '개발 도구', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'SUBMITTED', '맥도날드 역삼점', 9500, '식비', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'REQUEST_MORE_INFO', 'GS25 편의점', 4000, '기타', DATE_SUB(CURDATE(), INTERVAL 9 DAY), '음료 구매', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '교보문고', 22000, '비품', DATE_SUB(CURDATE(), INTERVAL 18 DAY), '기술 서적', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY));

-- 영수증 업로드 데이터 (제출된 지출 내역 중 일부)
-- MariaDB는 LIMIT을 IN 서브쿼리에서 지원하지 않으므로 직접 조건 사용
INSERT INTO `receipt_upload` (expense_id, uploaded_by, file_url, file_hash, mime_type, created_at, updated_at)
SELECT 
    e.id,
    e.user_id,
    CONCAT('/uploads/receipts/receipt_', e.id, '.jpg') as file_url,
    CONCAT('hash_', MD5(CONCAT(e.id, e.user_id))) as file_hash,
    'image/jpeg' as mime_type,
    e.created_at,
    NOW() as updated_at
FROM `expense` e
WHERE e.approval_status IN ('SUBMITTED', 'APPROVED', 'REJECTED', 'REQUEST_MORE_INFO')
ORDER BY e.id
LIMIT 12;

-- AI 추출 결과 데이터 (영수증이 있는 것 중 일부)
INSERT INTO `receipt_ai_extraction` (receipt_id, model_name, extracted_json, extracted_date, extracted_amount, extracted_merchant, extracted_category, confidence, created_at, updated_at)
SELECT 
    ru.id,
    'gpt-4-vision' as model_name,
    JSON_OBJECT(
        'date', DATE_FORMAT(e.receipt_date, '%Y-%m-%d'),
        'amount', e.amount,
        'merchant', e.merchant,
        'category', e.category
    ) as extracted_json,
    e.receipt_date as extracted_date,
    e.amount as extracted_amount,
    e.merchant as extracted_merchant,
    e.category as extracted_category,
    CAST(0.90 AS DECIMAL(5,2)) as confidence,
    ru.created_at,
    NOW() as updated_at
FROM `receipt_upload` ru
JOIN `expense` e ON ru.expense_id = e.id
LIMIT 8;

-- 결재 요청 데이터 (제출된 지출 내역에 대해)
INSERT INTO `approval_request` (request_type, ref_id, requester_id, approver_id, status_snapshot, created_at, updated_at)
SELECT 
    'EXPENSE' as request_type,
    e.id as ref_id,
    e.user_id as requester_id,
    (SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1) as approver_id,
    e.approval_status as status_snapshot,
    e.created_at,
    e.updated_at
FROM `expense` e
WHERE e.approval_status IN ('SUBMITTED', 'APPROVED', 'REJECTED', 'REQUEST_MORE_INFO')
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 결재 액션 로그 데이터
-- 각 결재 요청에 대해 제출 로그는 필수, 상태에 따라 추가 액션 로그 생성
INSERT INTO `approval_action_log` (approval_request_id, actor_id, action, message, created_at)
SELECT 
    ar.id,
    ar.requester_id as actor_id,
    'SUBMIT' as action,
    '지출 내역을 제출했습니다.' as message,
    ar.created_at
FROM `approval_request` ar
WHERE ar.request_type = 'EXPENSE';

-- 승인/반려/보완요청 로그 추가 (제출 이후 액션)
-- 승인: 사유 선택사항
INSERT INTO `approval_action_log` (approval_request_id, actor_id, action, message, created_at)
SELECT 
    ar.id,
    ar.approver_id as actor_id,
    'APPROVE' as action,
    '지출 내역을 승인했습니다.' as message,
    DATE_ADD(ar.created_at, INTERVAL 1 DAY) as created_at
FROM `approval_request` ar
WHERE ar.request_type = 'EXPENSE'
  AND ar.status_snapshot = 'APPROVED';

-- 반려: 사유 필수 (구체적인 반려 사유 포함)
INSERT INTO `approval_action_log` (approval_request_id, actor_id, action, message, created_at)
SELECT 
    ar.id,
    ar.approver_id as actor_id,
    'REJECT' as action,
    CASE 
        WHEN e.merchant LIKE '%맥도날드%' THEN '개인 용도의 지출로 판단되어 반려합니다. 업무 관련 지출만 승인 가능합니다.'
        WHEN e.merchant LIKE '%이마트%' AND e.amount > 40000 THEN '금액이 과도하여 추가 확인이 필요합니다. 구매 내역서를 첨부해주세요.'
        ELSE '지출 내역이 명세서 기준에 부합하지 않아 반려합니다. 재제출 시 보완 부탁드립니다.'
    END as message,
    DATE_ADD(ar.created_at, INTERVAL 1 DAY) as created_at
FROM `approval_request` ar
JOIN `expense` e ON ar.ref_id = e.id
WHERE ar.request_type = 'EXPENSE'
  AND ar.status_snapshot = 'REJECTED';

-- 보완요청: 사유 필수 (구체적인 보완 요청 사유 포함)
INSERT INTO `approval_action_log` (approval_request_id, actor_id, action, message, created_at)
SELECT 
    ar.id,
    ar.approver_id as actor_id,
    'REQUEST_MORE_INFO' as action,
    CASE 
        WHEN e.merchant LIKE '%맥도날드%' THEN '점심 식사 내역에 대해 업무 관련성 확인이 필요합니다. 회의 참석자 명단 또는 업무 관련 증빙을 추가해주세요.'
        WHEN e.merchant LIKE '%GS25%' THEN '구매한 품목의 상세 내역이 필요합니다. 영수증에 품목명이 명확히 보이도록 재촬영해주세요.'
        ELSE '추가 정보가 필요합니다. 지출 목적과 업무 관련성을 명확히 기재해주세요.'
    END as message,
    DATE_ADD(ar.created_at, INTERVAL 1 DAY) as created_at
FROM `approval_request` ar
JOIN `expense` e ON ar.ref_id = e.id
WHERE ar.request_type = 'EXPENSE'
  AND ar.status_snapshot = 'REQUEST_MORE_INFO';

-- 영수증 검증 데이터 (승인된 지출 내역 중 영수증이 있는 것들)
INSERT INTO `receipt_verification` (expense_id, verified_by, verified_merchant, verified_amount, verified_category, reason, created_at, updated_at)
SELECT 
    e.id,
    (SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1) as verified_by,
    e.merchant as verified_merchant,
    e.amount as verified_amount,
    e.category as verified_category,
    'AI 추출 결과와 일치합니다.' as reason,
    e.updated_at,
    NOW() as updated_at
FROM `expense` e
WHERE e.approval_status = 'APPROVED'
  AND EXISTS (SELECT 1 FROM `receipt_upload` ru WHERE ru.expense_id = e.id)
LIMIT 5;

