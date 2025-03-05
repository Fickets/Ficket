


INSERT IGNORE INTO refund_policy (cancellation_period, refund_fee_description, priority)
VALUES
    ('예매 후 7일 이내', '없음', 1),
    ('예매 후 8일~관람일 10일전까지', '장당 4,000원(티켓 금액의 10% 한도)', 2),
    ('관람일 9일전~7일전까지', '티켓 금액의 10%', 3),
    ('관람일 6일전~3일전까지', '티켓 금액의 20%', 4),
    ('관람일 2일전~1일전까지', '티켓 금액의 30%', 5);