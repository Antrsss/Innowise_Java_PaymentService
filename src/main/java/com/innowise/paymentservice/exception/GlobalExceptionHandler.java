package com.innowise.paymentservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String TIMESTAMP_KEY = "timestamp";
  private static final String MESSAGE_KEY = "exception";

  @ExceptionHandler(BaseServiceException.class)
  public ResponseEntity<Object> handleBaseException(BaseServiceException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(TIMESTAMP_KEY, LocalDateTime.now());
    body.put(MESSAGE_KEY, ex.getMessage());

    return new ResponseEntity<>(body, ex.getHttpStatus());
  }
}
