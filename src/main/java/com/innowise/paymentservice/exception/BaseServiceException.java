package com.innowise.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseServiceException extends RuntimeException {

  private final HttpStatus httpStatus;

  public BaseServiceException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
