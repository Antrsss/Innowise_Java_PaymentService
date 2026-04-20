package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
  Payment createPayment(Payment payment);
  List<Payment> getPaymentsByUserId(Long userId);
  List<Payment> getPaymentsByOrderId(Long orderId);
  List<Payment> getPaymentsByStatus(PaymentStatus status);

  BigDecimal findPaymentsTotalSumForAllUsersForDateRange(
      LocalDate startDate, LocalDate endDate);

  BigDecimal findPaymentsTotalSumForUserForDateRange(
      Long userId, LocalDate startDate, LocalDate endDate);
}
