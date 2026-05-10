package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.config.RandomApiProperties;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentDao paymentDao;
  private final WebClient randomNumberWebClient;
  private final RandomApiProperties apiProperties;
  private final PaymentProducer paymentProducer;

  @Override
  public Mono<Payment> createPayment(Payment payment) {
    return randomNumberWebClient.get()
        .uri(uriBuilder -> {
          uriBuilder.path(apiProperties.getPath());
          apiProperties.getDefaultParams().forEach(uriBuilder::queryParam);
          return uriBuilder.build();
        })

        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofSeconds(3))
        .map(response -> Integer.parseInt(response.trim()))
        .onErrorReturn(1)

        .flatMap(randomNumber -> {
          payment.setStatus(randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
          payment.setTimestamp(LocalDateTime.now());
          return paymentDao.insert(payment);
        })

        .onErrorResume(DuplicateKeyException.class,
            e -> Mono.error(new PaymentDuplicateException("Payment already exists")))

        .doOnNext(savedPayment ->
            paymentProducer.sendPaymentEvent(savedPayment.getOrderId(), savedPayment.getStatus())
        );
  }

  @Override
  public Flux<Payment> getPaymentsByUserId(Long userId) {
    return paymentDao.findByUserId(userId);
  }

  @Override
  public Flux<Payment> getPaymentsByOrderId(Long orderId) {
    return paymentDao.findByOrderId(orderId);
  }

  @Override
  public Flux<Payment> getPaymentsByStatus(PaymentStatus status) {
    return paymentDao.findByStatus(status);
  }

  @Override
  public Mono<BigDecimal> findPaymentsTotalSumForUserForDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    return paymentDao.findByUserIdAndTimestampBetween(userId, startDateTime, endDateTime)
        .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
        .map(p -> p.getPaymentAmount() != null ? p.getPaymentAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public Mono<BigDecimal> findPaymentsTotalSumForAllUsersForDateRange(LocalDate startDate, LocalDate endDate) {
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    return paymentDao.findAllByDateRange(startDateTime, endDateTime)
        .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
        .map(p -> p.getPaymentAmount() != null ? p.getPaymentAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
