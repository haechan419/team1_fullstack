-- SmartSpend 지출 정산 센터 과거 데이터 (회계 통계용)
-- 개발/테스트 환경에서만 사용하세요!
-- 이 파일은 data.sql 실행 후 실행하세요.

USE smartspenddb;

-- 과거 2-3개월치 지출 내역 데이터 추가 (APPROVED 상태 위주)
-- 회계 통계 페이지에서 사용할 데이터
INSERT INTO `expense` (user_id, approval_status, merchant, amount, category, receipt_date, description, created_at, updated_at)
VALUES
  -- USER001 (홍길동) - 개발팀 - 2개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '스타벅스 강남점', 15000, '식비', DATE_SUB(CURDATE(), INTERVAL 60 DAY), '팀 회의 커피', DATE_SUB(NOW(), INTERVAL 65 DAY), DATE_SUB(NOW(), INTERVAL 63 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '이마트', 45000, '비품', DATE_SUB(CURDATE(), INTERVAL 55 DAY), '사무용품 구매', DATE_SUB(NOW(), INTERVAL 58 DAY), DATE_SUB(NOW(), INTERVAL 56 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', 'GS25 편의점', 8000, '기타', DATE_SUB(CURDATE(), INTERVAL 50 DAY), '간식 구매', DATE_SUB(NOW(), INTERVAL 53 DAY), DATE_SUB(NOW(), INTERVAL 51 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '교보문고', 35000, '비품', DATE_SUB(CURDATE(), INTERVAL 45 DAY), '기술 서적', DATE_SUB(NOW(), INTERVAL 48 DAY), DATE_SUB(NOW(), INTERVAL 46 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '스타벅스 강남점', 18000, '식비', DATE_SUB(CURDATE(), INTERVAL 40 DAY), '고객 미팅', DATE_SUB(NOW(), INTERVAL 43 DAY), DATE_SUB(NOW(), INTERVAL 41 DAY)),
  
  -- USER001 - 1개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '이마트', 52000, '비품', DATE_SUB(CURDATE(), INTERVAL 35 DAY), '개발 도구', DATE_SUB(NOW(), INTERVAL 38 DAY), DATE_SUB(NOW(), INTERVAL 36 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '맥도날드 역삼점', 12000, '식비', DATE_SUB(CURDATE(), INTERVAL 30 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 33 DAY), DATE_SUB(NOW(), INTERVAL 31 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', 'GS25 편의점', 6000, '기타', DATE_SUB(CURDATE(), INTERVAL 28 DAY), '음료 구매', DATE_SUB(NOW(), INTERVAL 31 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '스타벅스 강남점', 20000, '식비', DATE_SUB(CURDATE(), INTERVAL 22 DAY), '코드 리뷰 미팅', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), 'APPROVED', '교보문고', 28000, '비품', DATE_SUB(CURDATE(), INTERVAL 20 DAY), '도서 구매', DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY)),
  
  -- USER002 (김철수) - 영업팀 - 2개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '스타벅스 강남점', 25000, '식비', DATE_SUB(CURDATE(), INTERVAL 58 DAY), '고객 미팅', DATE_SUB(NOW(), INTERVAL 61 DAY), DATE_SUB(NOW(), INTERVAL 59 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '이마트', 68000, '비품', DATE_SUB(CURDATE(), INTERVAL 52 DAY), '프레젠테이션 용품', DATE_SUB(NOW(), INTERVAL 55 DAY), DATE_SUB(NOW(), INTERVAL 53 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', 'GS25 편의점', 5000, '기타', DATE_SUB(CURDATE(), INTERVAL 48 DAY), '음료 구매', DATE_SUB(NOW(), INTERVAL 51 DAY), DATE_SUB(NOW(), INTERVAL 49 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '교보문고', 42000, '비품', DATE_SUB(CURDATE(), INTERVAL 42 DAY), '업무 서적', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 43 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '스타벅스 강남점', 22000, '식비', DATE_SUB(CURDATE(), INTERVAL 38 DAY), '고객 미팅', DATE_SUB(NOW(), INTERVAL 41 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
  
  -- USER002 - 1개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '이마트', 55000, '비품', DATE_SUB(CURDATE(), INTERVAL 32 DAY), '마케팅 자료', DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 33 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '맥도날드 역삼점', 10000, '식비', DATE_SUB(CURDATE(), INTERVAL 27 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', 'GS25 편의점', 4000, '기타', DATE_SUB(CURDATE(), INTERVAL 24 DAY), '간식 구매', DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '스타벅스 강남점', 30000, '식비', DATE_SUB(CURDATE(), INTERVAL 19 DAY), '고객 미팅', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), 'APPROVED', '교보문고', 38000, '비품', DATE_SUB(CURDATE(), INTERVAL 16 DAY), '업무 서적', DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY)),
  
  -- USER003 (이영희) - 마케팅팀 - 2개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '스타벅스 강남점', 18000, '식비', DATE_SUB(CURDATE(), INTERVAL 56 DAY), '팀 회의', DATE_SUB(NOW(), INTERVAL 59 DAY), DATE_SUB(NOW(), INTERVAL 57 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '이마트', 75000, '비품', DATE_SUB(CURDATE(), INTERVAL 50 DAY), '마케팅 용품', DATE_SUB(NOW(), INTERVAL 53 DAY), DATE_SUB(NOW(), INTERVAL 51 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', 'GS25 편의점', 9000, '기타', DATE_SUB(CURDATE(), INTERVAL 46 DAY), '간식 구매', DATE_SUB(NOW(), INTERVAL 49 DAY), DATE_SUB(NOW(), INTERVAL 47 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '교보문고', 48000, '비품', DATE_SUB(CURDATE(), INTERVAL 40 DAY), '마케팅 도서', DATE_SUB(NOW(), INTERVAL 43 DAY), DATE_SUB(NOW(), INTERVAL 41 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '스타벅스 강남점', 16000, '식비', DATE_SUB(CURDATE(), INTERVAL 36 DAY), '팀 회의', DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 37 DAY)),
  
  -- USER003 - 1개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '이마트', 62000, '비품', DATE_SUB(CURDATE(), INTERVAL 33 DAY), '마케팅 자료', DATE_SUB(NOW(), INTERVAL 36 DAY), DATE_SUB(NOW(), INTERVAL 34 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '맥도날드 역삼점', 13000, '식비', DATE_SUB(CURDATE(), INTERVAL 29 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 32 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', 'GS25 편의점', 7000, '기타', DATE_SUB(CURDATE(), INTERVAL 26 DAY), '음료 구매', DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '스타벅스 강남점', 24000, '식비', DATE_SUB(CURDATE(), INTERVAL 21 DAY), '팀 회의', DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), 'APPROVED', '교보문고', 36000, '비품', DATE_SUB(CURDATE(), INTERVAL 17 DAY), '마케팅 도서', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
  
  -- USER004 (박민수) - 개발팀 - 2개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '스타벅스 강남점', 14000, '식비', DATE_SUB(CURDATE(), INTERVAL 54 DAY), '코드 리뷰 미팅', DATE_SUB(NOW(), INTERVAL 57 DAY), DATE_SUB(NOW(), INTERVAL 55 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '이마트', 38000, '비품', DATE_SUB(CURDATE(), INTERVAL 48 DAY), '개발 도구', DATE_SUB(NOW(), INTERVAL 51 DAY), DATE_SUB(NOW(), INTERVAL 49 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', 'GS25 편의점', 6500, '기타', DATE_SUB(CURDATE(), INTERVAL 44 DAY), '음료 구매', DATE_SUB(NOW(), INTERVAL 47 DAY), DATE_SUB(NOW(), INTERVAL 45 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '교보문고', 32000, '비품', DATE_SUB(CURDATE(), INTERVAL 38 DAY), '기술 서적', DATE_SUB(NOW(), INTERVAL 41 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '스타벅스 강남점', 17000, '식비', DATE_SUB(CURDATE(), INTERVAL 34 DAY), '코드 리뷰 미팅', DATE_SUB(NOW(), INTERVAL 37 DAY), DATE_SUB(NOW(), INTERVAL 35 DAY)),
  
  -- USER004 - 1개월 전 데이터
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '이마트', 48000, '비품', DATE_SUB(CURDATE(), INTERVAL 31 DAY), '개발 도구', DATE_SUB(NOW(), INTERVAL 34 DAY), DATE_SUB(NOW(), INTERVAL 32 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '맥도날드 역삼점', 11000, '식비', DATE_SUB(CURDATE(), INTERVAL 28 DAY), '점심 식사', DATE_SUB(NOW(), INTERVAL 31 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', 'GS25 편의점', 5500, '기타', DATE_SUB(CURDATE(), INTERVAL 25 DAY), '간식 구매', DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '스타벅스 강남점', 19000, '식비', DATE_SUB(CURDATE(), INTERVAL 23 DAY), '코드 리뷰 미팅', DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), 'APPROVED', '교보문고', 29000, '비품', DATE_SUB(CURDATE(), INTERVAL 19 DAY), '기술 서적', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 과거 데이터에 대한 결재 요청 생성
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
WHERE e.approval_status = 'APPROVED'
  AND e.receipt_date < DATE_SUB(CURDATE(), INTERVAL 15 DAY)
  AND NOT EXISTS (
    SELECT 1 FROM `approval_request` ar 
    WHERE ar.request_type = 'EXPENSE' AND ar.ref_id = e.id
  )
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 과거 데이터에 대한 제출 로그 생성
INSERT INTO `approval_action_log` (approval_request_id, actor_id, action, message, created_at)
SELECT 
    ar.id,
    ar.requester_id as actor_id,
    'SUBMIT' as action,
    '지출 내역을 제출했습니다.' as message,
    ar.created_at
FROM `approval_request` ar
WHERE ar.request_type = 'EXPENSE'
  AND NOT EXISTS (
    SELECT 1 FROM `approval_action_log` aal 
    WHERE aal.approval_request_id = ar.id AND aal.action = 'SUBMIT'
  );

-- 과거 데이터에 대한 승인 로그 생성
INSERT INTO `approval_action_log` (approval_request_id, actor_id, action, message, created_at)
SELECT 
    ar.id,
    ar.approver_id as actor_id,
    'APPROVE' as action,
    '지출 내역을 승인했습니다.' as message,
    DATE_ADD(ar.created_at, INTERVAL 1 DAY) as created_at
FROM `approval_request` ar
WHERE ar.request_type = 'EXPENSE'
  AND ar.status_snapshot = 'APPROVED'
  AND NOT EXISTS (
    SELECT 1 FROM `approval_action_log` aal 
    WHERE aal.approval_request_id = ar.id AND aal.action = 'APPROVE'
  );

