package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentDto(

    @NotNull(message = "OrderId can't be null")
    Long orderId,

    @NotNull(message = "UserId can't be null")
    Long userId,

    @NotNull(message = "Payment amount can't be null")
    @Positive(message = "Payment amount must be positive")
    BigDecimal paymentAmount
) {}
