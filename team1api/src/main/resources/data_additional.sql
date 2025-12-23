-- SmartSpend 지출 정산 센터 추가 테스트 데이터
-- 개발/테스트 환경에서만 사용하세요!

USE smartspenddb;

-- 사원별 월간 지출 한도 데이터 (최근 6개월)
-- 각 USER 역할 사용자에 대해 최근 6개월의 예산 한도 데이터 생성
INSERT INTO `user_budget_monthly` (`user_id`, `year_month`, `monthly_limit`, `note`, `created_at`, `updated_at`)
VALUES
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 0 MONTH), '%Y-%m'), 2000000, NULL, NOW(), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m'), 1500000, NULL, DATE_SUB(NOW(), INTERVAL 2 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 3 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m'), 2500000, NULL, DATE_SUB(NOW(), INTERVAL 4 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER001' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 5 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 0 MONTH), '%Y-%m'), 2500000, NULL, NOW(), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m'), 2500000, NULL, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 2 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m'), 2500000, NULL, DATE_SUB(NOW(), INTERVAL 3 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 4 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER002' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m'), 1500000, NULL, DATE_SUB(NOW(), INTERVAL 5 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 0 MONTH), '%Y-%m'), 1500000, NULL, NOW(), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m'), 1500000, NULL, DATE_SUB(NOW(), INTERVAL 2 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m'), 1000000, NULL, DATE_SUB(NOW(), INTERVAL 3 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 4 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER003' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m'), 1500000, NULL, DATE_SUB(NOW(), INTERVAL 5 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 0 MONTH), '%Y-%m'), 2000000, NULL, NOW(), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m'), 2500000, NULL, DATE_SUB(NOW(), INTERVAL 2 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 3 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m'), 1500000, NULL, DATE_SUB(NOW(), INTERVAL 4 MONTH), NOW()),
  ((SELECT id FROM `user` WHERE employee_no = 'USER004' LIMIT 1), DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m'), 2000000, NULL, DATE_SUB(NOW(), INTERVAL 5 MONTH), NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();

-- 리포트 생성 작업 더미 데이터
INSERT INTO `report_job` (requested_by, scope_type, scope_value, period_start, period_end, template, report_type, status, created_at, updated_at)
VALUES
  ((SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1), 'ALL', NULL, DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE(), 'EXCEL', 'DETAIL_EXCEL', 'DONE', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
  ((SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1), 'DEPARTMENT_NAME', '개발팀', DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE(), 'PDF', 'PERSONAL_PDF', 'DONE', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
  ((SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1), 'ALL', NULL, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'EXCEL', 'DEPT_EXCEL', 'DONE', DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
  ((SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1), 'DEPARTMENT_NAME', '영업팀', DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE(), 'PDF', 'PERSONAL_PDF', 'DONE', DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
  ((SELECT id FROM `user` WHERE role = 'ADMIN' LIMIT 1), 'DEPARTMENT_NAME', '마케팅팀', DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE(), 'EXCEL', 'DETAIL_EXCEL', 'DONE', DATE_SUB(NOW(), INTERVAL 25 DAY), NOW());

-- 리포트 파일 더미 데이터 (위에서 생성한 report_job에 대한 파일)
INSERT INTO `report_file` (report_job_id, file_name, file_url, file_type, file_size, checksum, created_at)
SELECT 
    rj.id,
    CONCAT('report_', rj.id, '_', LOWER(rj.template), CASE WHEN rj.template = 'EXCEL' THEN '.xlsx' ELSE '.pdf' END) as file_name,
    CONCAT('/api/admin/accounting/reports/', rj.id, '/download') as file_url,
    CASE 
        WHEN rj.template = 'EXCEL' THEN 'XLSX'
        ELSE 'PDF'
    END as file_type,
    1024000 + (rj.id * 100000) as file_size,
    CONCAT('checksum_', MD5(CONCAT(rj.id, rj.created_at))) as checksum,
    rj.created_at
FROM `report_job` rj
WHERE rj.status = 'DONE';

