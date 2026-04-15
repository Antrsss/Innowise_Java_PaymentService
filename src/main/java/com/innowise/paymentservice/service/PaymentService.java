package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
  Payment createPayment(Payment payment);
  List<Payment> getPaymentsByUserId(Long userId);
  List<Payment> getPaymentsByOrderId(Long orderId);
  List<Payment> getPaymentsByStatus(String status);
  BigDecimal findPaymentsTotalSumForUserForDateRange(
      Long userId, LocalDateTime startDate, LocalDateTime endDate);
  BigDecimal findPaymentsTotalSumForAllUsersForDateRange(
      LocalDateTime startDate, LocalDateTime endDate);
}
