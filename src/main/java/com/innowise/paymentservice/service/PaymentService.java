package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentService {
  Mono<Payment> createPayment(Payment payment);
  Flux<Payment> getPaymentsByUserId(Long userId);
  Flux<Payment> getPaymentsByOrderId(Long orderId);
  Flux<Payment> getPaymentsByStatus(PaymentStatus status);

  Mono<BigDecimal> findPaymentsTotalSumForAllUsersForDateRange(
      LocalDate startDate, LocalDate endDate);

  Mono<BigDecimal> findPaymentsTotalSumForUserForDateRange(
      Long userId, LocalDate startDate, LocalDate endDate);
}
