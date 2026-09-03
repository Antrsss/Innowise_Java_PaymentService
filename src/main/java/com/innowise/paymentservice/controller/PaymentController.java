package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<PaymentResponseDto> createPayment(@RequestBody @NotNull PaymentDto paymentDto) {
    return Mono.just(paymentMapper.toPayment(paymentDto))
        .flatMap(paymentService::createPayment)
        .map(paymentMapper::toPaymentResponseDto);
  }

  @GetMapping("/user/{userId}")
  public Flux<PaymentResponseDto> getPaymentByUserId(@PathVariable @NotNull Long userId) {
    return paymentService.getPaymentsByUserId(userId)
        .map(paymentMapper::toPaymentResponseDto);
  }

  @GetMapping("/order/{orderId}")
  public Flux<PaymentResponseDto> getPaymentByOrderId(@PathVariable @NotNull Long orderId) {
    return paymentService.getPaymentsByOrderId(orderId)
        .map(paymentMapper::toPaymentResponseDto);
  }

  @GetMapping("/status/{status}")
  public Flux<PaymentResponseDto> getPaymentByStatus(@PathVariable @NotNull PaymentStatus status) {
    return paymentService.getPaymentsByStatus(status)
        .map(paymentMapper::toPaymentResponseDto);
  }

  @GetMapping("/total")
  public Mono<BigDecimal> getTotal(
      @RequestParam @NotNull LocalDate start,
      @RequestParam @NotNull LocalDate end) {
    return paymentService.findPaymentsTotalSumForAllUsersForDateRange(start, end);
  }

  @GetMapping("/total/{userId}")
  public Mono<BigDecimal> getTotalByUserId(
      @PathVariable @NotNull Long userId,
      @RequestParam @NotNull LocalDate start,
      @RequestParam @NotNull LocalDate end) {
    return paymentService.findPaymentsTotalSumForUserForDateRange(userId, start, end);
  }
}