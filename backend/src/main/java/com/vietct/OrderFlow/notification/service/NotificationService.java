package com.vietct.OrderFlow.notification.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface NotificationService {

    void notifyOrderPaid(UUID orderId, UUID userId, BigDecimal amount);
}
