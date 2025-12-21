package com.vietct.OrderFlow.common.lock;

import java.time.Duration;
import java.util.Optional;

public interface DistributedLockManager {

    Optional<String> tryAcquireLock(String name, Duration ttl);

    void releaseLock(String name, String lockValue);
}
