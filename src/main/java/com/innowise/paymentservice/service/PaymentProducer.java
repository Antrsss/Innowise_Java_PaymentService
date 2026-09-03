package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.PaymentStatus;

public interface PaymentProducer {
  void sendPaymentEvent(Long orderId, PaymentStatus status);
}
