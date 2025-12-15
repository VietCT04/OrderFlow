package com.vietct.OrderFlow.payment.service;

import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.payment.domain.Payment;

import java.math.BigDecimal;

public interface PaymentService {

    Payment processPayment(Order order, BigDecimal amount, String paymentMethod);
}
