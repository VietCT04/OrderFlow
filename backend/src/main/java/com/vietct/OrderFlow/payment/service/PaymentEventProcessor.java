package com.vietct.OrderFlow.payment.service;

import com.vietct.OrderFlow.payment.event.PaymentCompletedEvent;

public interface PaymentEventProcessor {

    void handlePaymentCompleted(PaymentCompletedEvent event);
}
