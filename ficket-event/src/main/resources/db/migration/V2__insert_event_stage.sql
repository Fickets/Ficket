INSERT IGNORE INTO event_stage (stage_id, stage_name, stage_size, sido, sigungu, street, event_stage_img, created_at,
                                last_modified_at, deleted_at)
VALUES (1, '스포츠장', 1122, '서울특별시', '구로구', '디지털로 54길 14',
        'https://ficket-event-content.s3.ap-northeast-2.amazonaws.com/stage/sportStage.png', NOW(), NOW(), NULL),
       (2, '콘서트장', 2064, '인천광역시', '권관구', '검단사거리 12길 15',
        'https://ficket-event-content.s3.ap-northeast-2.amazonaws.com/stage/concertStage.png', NOW(), NOW(), NULL),
       (3, '뮤지컬장', 579, '서울특별시', '종로구', '세종로 13길 24',
        'https://ficket-event-content.s3.ap-northeast-2.amazonaws.com/stage/musical.png', NOW(), NOW(), NULL);
