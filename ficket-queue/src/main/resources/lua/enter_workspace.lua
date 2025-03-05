local key = KEYS[1]
local value = "active"  -- 값은 항상 "active"로 설정
local ttl = 1200  -- TTL을 20분(1200초)으로 설정

if redis.call('EXISTS', key) == 0 then
    redis.call('SET', key, value, 'EX', ttl)
    return 1
else
    return 0
end