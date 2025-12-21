package com.vietct.OrderFlow.common.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisDistributedLockManager implements DistributedLockManager {

    private static final String LOCK_KEY_PREFIX = "lock:";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> unlockScript;

    public RedisDistributedLockManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setResultType(Long.class);
        this.unlockScript.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "  return redis.call('del', KEYS[1]) " +
                        "else " +
                        "  return 0 " +
                        "end"
        );
    }

    @Override
    public Optional<String> tryAcquireLock(String name, Duration ttl) {
        String key = lockKey(name);
        String value = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, ttl);

        if (Boolean.TRUE.equals(success)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public void releaseLock(String name, String lockValue) {
        String key = lockKey(name);
        redisTemplate.execute(
                unlockScript,
                Collections.singletonList(key),
                lockValue
        );
    }

    private String lockKey(String name) {
        return LOCK_KEY_PREFIX + name;
    }
}
