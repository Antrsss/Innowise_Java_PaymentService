package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
    Long id,
    Long orderId,
    Long userId,
    PaymentStatus status,
    LocalDateTime timestamp,
    BigDecimal paymentAmount
) {
}
