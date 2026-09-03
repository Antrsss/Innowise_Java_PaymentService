package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.service.PaymentProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentProducerImpl implements PaymentProducer {

  private static final String ORDER_ID = "orderId";
  private static final String PAYMENT_STATUS = "paymentStatus";
  private static final String PAYMENT_TOPIC = "CREATE_PAYMENT";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void sendPaymentEvent(Long orderId, PaymentStatus status) {
    Map<String, Object> payload = new HashMap<>();
    payload.put(ORDER_ID, orderId);
    payload.put(PAYMENT_STATUS, status);

    kafkaTemplate.send(PAYMENT_TOPIC, payload);
  }
}
