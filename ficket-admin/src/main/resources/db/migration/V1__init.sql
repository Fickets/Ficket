-- Admin 테이블 생성
CREATE TABLE IF NOT EXISTS admin
(
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ID       VARCHAR(255) NOT NULL,
    PW       VARCHAR(255) NOT NULL,
    NAME     VARCHAR(255) NOT NULL,
    ROLE     VARCHAR(255) NOT NULL
);

-- Account 테이블 생성
CREATE TABLE IF NOT EXISTS account
(
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    balance    DECIMAL(19, 2) NOT NULL
);

-- Membership 테이블 생성
CREATE TABLE IF NOT EXISTS membership
(
    membership_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade         VARCHAR(50)    NOT NULL,
    benefit       DECIMAL(19, 2) NOT NULL,
    baseline      DECIMAL(19, 2) NOT NULL
);

-- Company 테이블 생성
CREATE TABLE IF NOT EXISTS company
(
    company_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name  VARCHAR(50)    NOT NULL,
    revenue       DECIMAL(19, 2) NOT NULL,
    account_id    BIGINT         NOT NULL,
    membership_id BIGINT         NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE CASCADE,
    FOREIGN KEY (membership_id) REFERENCES membership (membership_id) ON DELETE CASCADE
);

-- SettlementRecord 테이블 생성
CREATE TABLE IF NOT EXISTS settlement_record
(
    settlement_record_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_net_supply_amount DECIMAL(19, 2) NOT NULL,
    total_vat               DECIMAL(19, 2) NOT NULL,
    total_supply_amount     DECIMAL(19, 2) NOT NULL,
    total_service_fee       DECIMAL(19, 2) NOT NULL,
    total_settlement_value  DECIMAL(19, 2) NOT NULL,
    total_refund_value      DECIMAL(19, 2) NOT NULL,
    settlement_status       VARCHAR(50),
    event_id                BIGINT         NOT NULL,

    created_at              DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at              DATETIME(6)    NULL
);

-- Settlement 테이블 생성
CREATE TABLE IF NOT EXISTS settlement
(
    settlement_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    net_supply_amount    DECIMAL(19, 2) NOT NULL,
    vat                  DECIMAL(19, 2) NOT NULL,
    supply_value         DECIMAL(19, 2) NOT NULL,
    service_fee          DECIMAL(19, 2) NOT NULL,
    refund_value         DECIMAL(19, 2) NOT NULL,
    settlement_value     DECIMAL(19, 2) NOT NULL,
    settlement_status    VARCHAR(50)    NOT NULL,
    order_id             BIGINT         NOT NULL,
    settlement_record_id BIGINT         NOT NULL,

    created_at           DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at           DATETIME(6)    NULL,

    FOREIGN KEY (settlement_record_id) REFERENCES settlement_record (settlement_record_id) ON DELETE CASCADE
);
