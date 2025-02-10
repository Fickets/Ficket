-- Ticket 테이블 생성
CREATE TABLE IF NOT EXISTS ticket
(
    ticket_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    viewing_status    VARCHAR(50) NOT NULL, -- Enum (문자열 저장)
    event_schedule_id BIGINT      NOT NULL,

    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at        DATETIME(6) NULL
);

-- Orders 테이블 생성
CREATE TABLE IF NOT EXISTS orders
(
    order_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id       VARCHAR(255)   NOT NULL,
    order_price      DECIMAL(19, 2) NOT NULL,
    order_status     VARCHAR(50)    NOT NULL,        -- Enum (문자열 저장)
    refund_price     DECIMAL(19, 2) NOT NULL DEFAULT 0,
    user_id          BIGINT         NOT NULL,
    ticket_id        BIGINT         NOT NULL UNIQUE, -- 1:1 관계 (Foreign Key)

    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at       DATETIME(6)    NULL,

    FOREIGN KEY (ticket_id) REFERENCES ticket (ticket_id) ON DELETE CASCADE
);

-- Refund_Policy 테이블 생성
CREATE TABLE IF NOT EXISTS refund_policy
(
    id                     INT AUTO_INCREMENT PRIMARY KEY,
    cancellation_period    VARCHAR(255) NOT NULL COMMENT '취소 기간',
    refund_fee_description VARCHAR(255) NOT NULL COMMENT '취소 수수료 설명',
    priority               INT          NOT NULL COMMENT '우선순위 (적용 순서 정의)',
    UNIQUE KEY (priority)
) COMMENT ='환불 정책 테이블';