package com.innowise.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class PaymentValidationException extends BaseServiceException {
  public PaymentValidationException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
