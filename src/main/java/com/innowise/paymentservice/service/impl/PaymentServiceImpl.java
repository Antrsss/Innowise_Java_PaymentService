package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentDuplicateException;
import com.innowise.paymentservice.service.PaymentProducer;
import com.innowise.paymentservice.service.PaymentService;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentDao paymentDao;
  private final WebClient randomNumberWebClient;
  private final PaymentProducer paymentProducer;

  @Override
  public Payment createPayment(Payment payment) {

    Integer randomNumber = randomNumberWebClient.get()
        .uri(uriBuilder -> uriBuilder.path("/integers/").queryParam("num", 1).build())
        .retrieve()
        .bodyToMono(String.class)
        .map(String::trim)
        .map(Integer::parseInt)
        .onErrorReturn(1)
        .block();

    if (randomNumber != null && randomNumber % 2 == 0) {
      payment.setStatus(PaymentStatus.SUCCESS);
    } else {
      payment.setStatus(PaymentStatus.FAILED);
    }

    payment.setTimestamp(LocalDateTime.now());

    try {
      Payment savedPayment = paymentDao.insert(payment);
      paymentProducer.sendPaymentEvent(savedPayment.getOrderId(), savedPayment.getStatus());

      return savedPayment;

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
  public List<Payment> getPaymentsByStatus(PaymentStatus status) {
    return paymentDao.findByStatus(status);
  }

  @Override
  public BigDecimal findPaymentsTotalSumForUserForDateRange(Long userId, LocalDate startDate, LocalDate endDate) {

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    List<Payment> payments = paymentDao
        .findByUserIdAndTimestampBetween(userId, startDateTime, endDateTime);

    return payments.stream()
        .filter(p -> PaymentStatus.SUCCESS
            .equals(p.getStatus())
        )
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
        .filter(p -> PaymentStatus.SUCCESS
            .equals(p.getStatus())
        )
        .map(Payment::getPaymentAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
