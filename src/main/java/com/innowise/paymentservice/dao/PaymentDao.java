package com.innowise.paymentservice.dao;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface PaymentDao extends ReactiveMongoRepository<Payment, Long> {
  Flux<Payment> findByUserId(Long userId);
  Flux<Payment> findByOrderId(Long orderId);
  Flux<Payment> findByStatus(PaymentStatus status);

  @Query("{ 'user_id': ?0, 'timestamp': { $gte: ?1, $lte: ?2 }, 'status': 'COMPLETED' }")
  Flux<Payment> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

  @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 }, 'status': 'COMPLETED' }")
  Flux<Payment> findAllByDateRange(LocalDateTime start, LocalDateTime end);
}
