-- SmartSpend 지출 정산 센터 (2번 파트) 데이터베이스 스키마
-- 명세서 기준으로 작성

-- 사용자 테이블 (공통)
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_no` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) UNIQUE,
    `password` VARCHAR(255),
    `name` VARCHAR(100) NOT NULL,
    `department_name` VARCHAR(100),
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_role` (`role`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 지출 내역 테이블
CREATE TABLE IF NOT EXISTS `expense` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `approval_status` VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    `merchant` VARCHAR(150),
    `amount` INT NOT NULL,
    `category` VARCHAR(50),
    `receipt_date` DATE NOT NULL,
    `receipt_image_url` VARCHAR(255),
    `description` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX `idx_user_receipt_date` (`user_id`, `receipt_date`),
    INDEX `idx_approval_status_updated` (`approval_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 영수증 업로드 테이블
CREATE TABLE IF NOT EXISTS `receipt_upload` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `expense_id` BIGINT NOT NULL UNIQUE,
    `uploaded_by` BIGINT NOT NULL,
    `file_url` VARCHAR(255) NOT NULL,
    `file_hash` VARCHAR(64),
    `mime_type` VARCHAR(50),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`expense_id`) REFERENCES `expense`(`id`),
    FOREIGN KEY (`uploaded_by`) REFERENCES `user`(`id`),
    INDEX `idx_expense_id` (`expense_id`),
    INDEX `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 추출 결과 테이블
CREATE TABLE IF NOT EXISTS `receipt_ai_extraction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `receipt_id` BIGINT NOT NULL UNIQUE,
    `model_name` VARCHAR(50) NOT NULL,
    `extracted_json` JSON NOT NULL,
    `extracted_date` DATE,
    `extracted_amount` INT,
    `extracted_merchant` VARCHAR(150),
    `extracted_category` VARCHAR(50),
    `confidence` DECIMAL(5,2),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`receipt_id`) REFERENCES `receipt_upload`(`id`),
    INDEX `idx_receipt_id` (`receipt_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 영수증 검증 테이블
CREATE TABLE IF NOT EXISTS `receipt_verification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `expense_id` BIGINT NOT NULL UNIQUE,
    `verified_by` BIGINT NOT NULL,
    `verified_merchant` VARCHAR(150),
    `verified_amount` INT,
    `verified_category` VARCHAR(50),
    `reason` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`expense_id`) REFERENCES `expense`(`id`),
    FOREIGN KEY (`verified_by`) REFERENCES `user`(`id`),
    INDEX `idx_verified_by_created` (`verified_by`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 결재 요청 테이블
CREATE TABLE IF NOT EXISTS `approval_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `request_type` VARCHAR(30) NOT NULL,
    `ref_id` BIGINT NOT NULL,
    `requester_id` BIGINT NOT NULL,
    `approver_id` BIGINT,
    `status_snapshot` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`requester_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`approver_id`) REFERENCES `user`(`id`),
    INDEX `idx_request_type_ref` (`request_type`, `ref_id`),
    INDEX `idx_approver_status` (`approver_id`, `status_snapshot`),
    INDEX `idx_status_updated` (`status_snapshot`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 결재 액션 로그 테이블
CREATE TABLE IF NOT EXISTS `approval_action_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `approval_request_id` BIGINT NOT NULL,
    `actor_id` BIGINT NOT NULL,
    `action` VARCHAR(30) NOT NULL,
    `message` TEXT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`approval_request_id`) REFERENCES `approval_request`(`id`),
    FOREIGN KEY (`actor_id`) REFERENCES `user`(`id`),
    INDEX `idx_approval_request_created` (`approval_request_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사원별 월간 지출 한도 테이블 (공통)
CREATE TABLE IF NOT EXISTS `user_budget_monthly` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `year_month` CHAR(7) NOT NULL,
    `monthly_limit` INT NOT NULL,
    `note` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_user_year_month` (`user_id`, `year_month`),
    INDEX `idx_user_year_month` (`user_id`, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 리포트 생성 작업 테이블 (공통)
CREATE TABLE IF NOT EXISTS `report_job` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `requested_by` BIGINT NOT NULL,
    `scope_type` VARCHAR(30) NOT NULL,
    `scope_value` VARCHAR(100),
    `period_start` DATE NOT NULL,
    `period_end` DATE NOT NULL,
    `template` VARCHAR(50) NOT NULL,
    `report_type` VARCHAR(50) NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`requested_by`) REFERENCES `user`(`id`),
    INDEX `idx_requested_by_created` (`requested_by`, `created_at`),
    INDEX `idx_status_updated` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 리포트 파일 히스토리 테이블 (공통)
CREATE TABLE IF NOT EXISTS `report_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `report_job_id` BIGINT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_url` VARCHAR(255) NOT NULL,
    `file_type` VARCHAR(20) NOT NULL,
    `file_size` BIGINT,
    `checksum` VARCHAR(64),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`report_job_id`) REFERENCES `report_job`(`id`),
    INDEX `idx_report_job_id` (`report_job_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

