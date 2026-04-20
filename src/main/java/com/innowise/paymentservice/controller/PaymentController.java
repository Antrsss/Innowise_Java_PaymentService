package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  @PostMapping
  public ResponseEntity<PaymentResponseDto> createPayment(
      @RequestBody @NotNull
      PaymentDto paymentDto
  ) {

    Payment payment = paymentMapper.toPayment(paymentDto);
    Payment savedPayment = paymentService.createPayment(payment);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(paymentMapper.toPaymentResponseDto(savedPayment));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentResponseDto>> getPaymentByUserId(
      @PathVariable @NotNull
      Long userId
  ) {
    List<Payment> payments = paymentService.getPaymentsByUserId(userId);

    return ResponseEntity
        .ok(paymentMapper.toPaymentResponseDtoList(payments));
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<List<PaymentResponseDto>> getPaymentByOrderId(
      @PathVariable @NotNull
      Long orderId
  ) {
    List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);

    return ResponseEntity
        .ok(paymentMapper.toPaymentResponseDtoList(payments));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<PaymentResponseDto>> getPaymentByStatus(
      @PathVariable @NotNull
      PaymentStatus status
  ) {
    List<Payment> payments = paymentService.getPaymentsByStatus(status);

    return ResponseEntity
        .ok(paymentMapper.toPaymentResponseDtoList(payments));
  }

  @GetMapping("/total")
  public ResponseEntity<BigDecimal> getTotal(
      @RequestParam @NotNull LocalDate start,
      @RequestParam @NotNull LocalDate end) {

    return ResponseEntity.ok(paymentService
        .findPaymentsTotalSumForAllUsersForDateRange(start, end));
  }

  @GetMapping("/total/{userId}")
  public ResponseEntity<BigDecimal> getTotalByUserId(
      @PathVariable @NotNull Long userId,
      @RequestParam @NotNull LocalDate start,
      @RequestParam @NotNull LocalDate end
  ) {

    return ResponseEntity.ok(paymentService
        .findPaymentsTotalSumForUserForDateRange(userId, start, end));
  }
}
