-- Event 테이블 생성
CREATE TABLE IF NOT EXISTS event
(
    event_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id          BIGINT       NOT NULL,
    company_id        BIGINT       NOT NULL,
    stage_id          BIGINT       NOT NULL,
    age               VARCHAR(50)  NOT NULL,
    content           TEXT         NOT NULL,
    title             VARCHAR(100) NOT NULL,
    sub_title         VARCHAR(100) NOT NULL,
    ticketing_time    TIMESTAMP    NOT NULL,
    running_time      INT          NOT NULL,
    reservation_limit INT          NOT NULL,

    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at        DATETIME(6)  NULL,

    INDEX idx_event_id (event_id)
);

-- EventGenre 테이블 생성 (ManyToMany 관계)
CREATE TABLE IF NOT EXISTS event_genre
(
    event_genre_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id       BIGINT      NOT NULL,
    genre          VARCHAR(50) NOT NULL,

    FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE
);

-- EventImage 테이블 생성
CREATE TABLE IF NOT EXISTS event_image
(
    event_img_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id            BIGINT      NOT NULL,
    poster_origin_url   VARCHAR(255),
    poster_mobile_url   VARCHAR(255),
    poster_pc_url       VARCHAR(255),
    poster_pc_main1url VARCHAR(255),
    poster_pc_main2url VARCHAR(255),
    banner_origin_url   VARCHAR(255),
    banner_pc_url       VARCHAR(255),
    banner_mobile_url   VARCHAR(255),

    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at          DATETIME(6) NULL,

    FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE
);

-- EventSchedule 테이블 생성
CREATE TABLE IF NOT EXISTS event_schedule
(
    event_schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id          BIGINT      NOT NULL,
    round             INT         NOT NULL,
    event_date        TIMESTAMP   NOT NULL,

    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at        DATETIME(6) NULL,

    FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE
);

-- EventStage 테이블 생성
CREATE TABLE IF NOT EXISTS event_stage
(
    stage_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    stage_name      VARCHAR(255) NOT NULL,
    stage_size      INT          NOT NULL,
    sido            VARCHAR(100) NOT NULL,
    sigungu         VARCHAR(100) NOT NULL,
    street          VARCHAR(255) NOT NULL,
    event_stage_img VARCHAR(255)
);

-- StagePartition 테이블 생성
CREATE TABLE IF NOT EXISTS stage_partition
(
    partition_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id         BIGINT         NOT NULL,
    partition_name   VARCHAR(255)   NOT NULL,
    partition_price  DECIMAL(19, 2) NOT NULL,

    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at       DATETIME(6)    NULL,

    FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE
);

-- StageSeat 테이블 생성
CREATE TABLE IF NOT EXISTS stage_seat
(
    seat_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    stage_id BIGINT      NOT NULL,
    x        DOUBLE      NOT NULL,
    y        DOUBLE      NOT NULL,
    seat_col VARCHAR(50) NOT NULL,
    seat_row VARCHAR(50) NOT NULL,

    FOREIGN KEY (stage_id) REFERENCES event_stage (stage_id) ON DELETE CASCADE
);

-- SeatMapping 테이블 생성
CREATE TABLE IF NOT EXISTS seat_mapping
(
    seat_mapping_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id         BIGINT NULL,
    partition_id      BIGINT NOT NULL,
    seat_id           BIGINT NOT NULL,
    event_schedule_id BIGINT NOT NULL,

    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at       DATETIME(6)    NULL,

    FOREIGN KEY (partition_id) REFERENCES stage_partition (partition_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES stage_seat (seat_id) ON DELETE CASCADE,
    FOREIGN KEY (event_schedule_id) REFERENCES event_schedule (event_schedule_id) ON DELETE CASCADE
);

-- FailedItem 테이블 생성
CREATE TABLE IF NOT EXISTS failed_item
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id          BIGINT       NOT NULL,
    reason           VARCHAR(255) NOT NULL,
    status           ENUM('PENDING', 'RETRIED', 'SUCCESS')  NOT NULL,

    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at       DATETIME(6)  NULL
);
