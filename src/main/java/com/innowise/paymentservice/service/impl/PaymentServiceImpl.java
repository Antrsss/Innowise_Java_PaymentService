package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.exception.PaymentDuplicateException;
import com.innowise.paymentservice.exception.PaymentValidationException;
import com.innowise.paymentservice.service.PaymentService;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentDao paymentDao;

  @Override
  public Payment createPayment(Payment payment) {
    try {
      return paymentDao.insert(payment);
    } catch (DuplicateKeyException e) {
      throw new PaymentDuplicateException("Payment already exists");
    }
  }

  @Override
  public List<Payment> getPaymentsByUserId(Long userId) {
    if (userId == null) {
      throw new PaymentValidationException("userId is null");
    }
    return paymentDao.findByUserId(userId);
  }

  @Override
  public List<Payment> getPaymentsByOrderId(Long orderId) {
    if (orderId == null) {
      throw new PaymentValidationException("orderId is null");
    }
    return paymentDao.findByOrderId(orderId);
  }

  @Override
  public List<Payment> getPaymentsByStatus(String status) {
    if (status == null) {
      throw new PaymentValidationException("status is null");
    }
    return paymentDao.findByStatus(status);
  }

  @Override
  public BigDecimal findPaymentsTotalSumForUserForDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
    if (userId == null || startDate == null || endDate == null) {
      throw new PaymentValidationException("userId or startDate or endDate is null");
    }

    List<Payment> payments = paymentDao
        .findByUserIdAndTimestampBetween(userId, startDate, endDate);

    return payments.stream()
        .filter(p -> "COMPLETED".equals(p.getStatus()))
        .map(Payment::getPaymentAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public BigDecimal findPaymentsTotalSumForAllUsersForDateRange(
      LocalDateTime startDate, LocalDateTime endDate) {

    if (startDate == null || endDate == null) {
      throw new PaymentValidationException("startDate or endDate is null");
    }

    List<Payment> payments = paymentDao.findAllByDateRange(startDate, endDate);

    return payments.stream()
        .filter(p -> "COMPLETED".equals(p.getStatus()))
        .map(Payment::getPaymentAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
