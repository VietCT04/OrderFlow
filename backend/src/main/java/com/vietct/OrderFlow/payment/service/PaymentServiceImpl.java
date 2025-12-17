package com.vietct.OrderFlow.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.outbox.domain.OutboxEvent;
import com.vietct.OrderFlow.outbox.repository.OutboxEventRepository;
import com.vietct.OrderFlow.payment.domain.Payment;
import com.vietct.OrderFlow.payment.domain.PaymentStatus;
import com.vietct.OrderFlow.payment.event.PaymentCompletedEvent;
import com.vietct.OrderFlow.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final String AGGREGATE_TYPE_PAYMENT = "PAYMENT";
    private static final String EVENT_TYPE_PAYMENT_COMPLETED = "PAYMENT_COMPLETED";

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final PaymentEventProcessor paymentEventProcessor;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper,
                              PaymentEventProcessor paymentEventProcessor) {
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.paymentEventProcessor = paymentEventProcessor;
    }

    @Override
    @Transactional
    public Payment processPayment(Order order, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                savedPayment.getId(),
                order.getId(),
                amount,
                paymentMethod,
                Instant.now()
        );

        paymentEventProcessor.handlePaymentCompleted(event);

        String payloadJson = toJson(event);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType(AGGREGATE_TYPE_PAYMENT);
        outboxEvent.setAggregateId(savedPayment.getId());
        outboxEvent.setEventType(EVENT_TYPE_PAYMENT_COMPLETED);
        outboxEvent.setPayload(payloadJson);

        outboxEventRepository.save(outboxEvent);

        log.info("Payment {} processed, outbox event created", savedPayment.getId());

        return savedPayment;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event payload", e);
        }
    }
}
