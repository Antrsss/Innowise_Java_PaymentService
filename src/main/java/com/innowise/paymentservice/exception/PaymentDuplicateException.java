package com.innowise.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class PaymentDuplicateException extends BaseServiceException {
  public PaymentDuplicateException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
