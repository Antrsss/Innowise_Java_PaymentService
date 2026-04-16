package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.exception.PaymentDuplicateException;
import com.innowise.paymentservice.service.PaymentService;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentDao paymentDao;

  @Override
  public Payment createPayment(Payment payment) {
    payment.setStatus("PENDING");
    payment.setTimestamp(LocalDateTime.now());

    try {
      return paymentDao.insert(payment);
    } catch (DuplicateKeyException e) {
      throw new PaymentDuplicateException("Payment already exists");
    }
  }

  @Override
  public List<Payment> getPaymentsByUserId(Long userId) {
    return paymentDao.findByUserId(userId);
  }

  @Override
  public List<Payment> getPaymentsByOrderId(Long orderId) {
    return paymentDao.findByOrderId(orderId);
  }

  @Override
  public List<Payment> getPaymentsByStatus(String status) {
    return paymentDao.findByStatus(status);
  }

  @Override
  public BigDecimal findPaymentsTotalSumForUserForDateRange(Long userId, LocalDate startDate, LocalDate endDate) {

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    List<Payment> payments = paymentDao
        .findByUserIdAndTimestampBetween(userId, startDateTime, endDateTime);

    return payments.stream()
        .filter(p -> "COMPLETED".equals(p.getStatus()))
        .map(Payment::getPaymentAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public BigDecimal findPaymentsTotalSumForAllUsersForDateRange(
      LocalDate startDate, LocalDate endDate) {

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    List<Payment> payments = paymentDao.findAllByDateRange(startDateTime, endDateTime);

    return payments.stream()
        .filter(p -> "COMPLETED".equals(p.getStatus()))
        .map(Payment::getPaymentAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
