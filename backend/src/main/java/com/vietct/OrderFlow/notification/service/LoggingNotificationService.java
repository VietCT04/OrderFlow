package com.vietct.OrderFlow.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LoggingNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void notifyOrderPaid(UUID orderId, UUID userId, BigDecimal amount) {
        log.info("Notification: orderId={} for userId={} has been PAID, amount={}",
                orderId, userId, amount);
    }
}
