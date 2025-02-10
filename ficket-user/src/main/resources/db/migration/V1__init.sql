CREATE TABLE IF NOT EXISTS user
(
    user_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    birth            INT          NULL,
    gender           TINYINT      NULL,
    user_name        VARCHAR(255) NULL,
    social_id        BIGINT       NOT NULL,
    state            TINYINT      NOT NULL,

    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at       DATETIME(6)  NULL
);