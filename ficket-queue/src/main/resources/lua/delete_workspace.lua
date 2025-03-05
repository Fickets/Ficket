local workspaceKey = KEYS[1]
local result = redis.call('del', workspaceKey)
if result == 1 then
    return 1
else
    return 0
end
