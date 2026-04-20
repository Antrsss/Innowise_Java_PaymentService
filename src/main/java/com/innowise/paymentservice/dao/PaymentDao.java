package com.innowise.paymentservice.dao;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentDao extends MongoRepository<Payment, Long> {
  List<Payment> findByUserId(Long userId);
  List<Payment> findByOrderId(Long orderId);
  List<Payment> findByStatus(PaymentStatus status);

  @Query("{ 'user_id': ?0, 'timestamp': { $gte: ?1, $lte: ?2 }, 'status': 'COMPLETED' }")
  List<Payment> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

  @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 }, 'status': 'COMPLETED' }")
  List<Payment> findAllByDateRange(LocalDateTime start, LocalDateTime end);
}
