package com.vietct.OrderFlow.payment.service;

import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.domain.OrderStatus;
import com.vietct.OrderFlow.order.repository.OrderRepository;
import com.vietct.OrderFlow.payment.domain.Payment;
import com.vietct.OrderFlow.payment.domain.PaymentStatus;
import com.vietct.OrderFlow.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Payment processPayment(Order order, BigDecimal amount, String paymentMethod) {
        // Sprint 2: mock payment that always succeeds
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return savedPayment;
    }
}
