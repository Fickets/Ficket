CREATE TABLE IF NOT EXISTS `membership`
(
    `baseline`      DECIMAL(38, 2)                       DEFAULT NULL, -- 기준 금액
    `benefit`       DECIMAL(38, 2)                       DEFAULT NULL, -- 혜택 비율
    `membership_id` BIGINT NOT NULL AUTO_INCREMENT,                    -- 멤버십 ID
    `grade`         ENUM ('BASIC','SILVER','GOLD','VIP') DEFAULT NULL, -- 등급
    PRIMARY KEY (`membership_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


INSERT IGNORE INTO `membership` (`baseline`, `benefit`, `membership_id`, `grade`)
VALUES (0.00, 0.00, 1, 'BASIC'),
       (50000000.00, 0.05, 2, 'SILVER'),
       (100000000.00, 0.10, 3, 'GOLD'),
       (150000000.00, 0.15, 4, 'VIP');