package com.vietct.OrderFlow.payment.service;

import com.vietct.OrderFlow.notification.service.NotificationService;
import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.domain.OrderStatus;
import com.vietct.OrderFlow.order.exception.OrderNotFoundException;
import com.vietct.OrderFlow.order.repository.OrderRepository;
import com.vietct.OrderFlow.payment.event.PaymentCompletedEvent;
import com.vietct.OrderFlow.payment.idempotence.ProcessedPaymentEvent;
import com.vietct.OrderFlow.payment.idempotence.ProcessedPaymentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentEventProcessorImpl implements PaymentEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProcessorImpl.class);
    private static final String EVENT_TYPE_PAYMENT_COMPLETED = "PAYMENT_COMPLETED";

    private final ProcessedPaymentEventRepository processedPaymentEventRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public PaymentEventProcessorImpl(ProcessedPaymentEventRepository processedPaymentEventRepository,
                                     OrderRepository orderRepository,
                                     NotificationService notificationService) {
        this.processedPaymentEventRepository = processedPaymentEventRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        UUID paymentId = event.paymentId();

        // Idempotence check
        if (processedPaymentEventRepository.existsById(paymentId)) {
            log.info("PaymentCompletedEvent already processed for paymentId={}, skipping", paymentId);
            return;
        }

        UUID orderId = event.orderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            log.info("Order {} marked as PAID due to payment {}", orderId, paymentId);
        } else {
            log.info("Order {} already PAID when handling payment {}", orderId, paymentId);
        }

        ProcessedPaymentEvent processed = new ProcessedPaymentEvent(
                paymentId,
                EVENT_TYPE_PAYMENT_COMPLETED,
                Instant.now()
        );
        processedPaymentEventRepository.save(processed);

        notificationService.notifyOrderPaid(order.getId(), order.getUserId(), event.amount());
    }
}
