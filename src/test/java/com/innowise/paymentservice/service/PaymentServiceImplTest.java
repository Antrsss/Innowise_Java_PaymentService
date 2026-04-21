package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentDuplicateException;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import com.mongodb.DuplicateKeyException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcernResult;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock private PaymentDao paymentDao;
  @Mock private PaymentProducer paymentProducer;
  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec uriSpec;
  @Mock private WebClient.RequestHeadersSpec headersSpec;
  @Mock private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private PaymentServiceImpl paymentService;

  @Test
  void shouldSetFailedStatus_WhenRandomNumberIsOdd() {
    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("3"));

    Payment payment = new Payment();
    payment.setOrderId(1L);
    when(paymentDao.insert(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    Payment result = paymentService.createPayment(payment);

    assertEquals(PaymentStatus.FAILED, result.getStatus());
    verify(paymentProducer).sendPaymentEvent(any(), eq(PaymentStatus.FAILED));
  }

  @Test
  void shouldSetSuccessStatus_WhenRandomNumberIsEven() {
    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("4"));

    Payment payment = new Payment();
    payment.setOrderId(100L);
    when(paymentDao.insert(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    Payment result = paymentService.createPayment(payment);

    assertEquals(PaymentStatus.SUCCESS, result.getStatus());
    verify(paymentProducer).sendPaymentEvent(100L, PaymentStatus.SUCCESS);
  }

  @Test
  void createPayment_ShouldThrowDuplicateException_WhenPaymentAlreadyExists() {
    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("2"));

    DuplicateKeyException mongoException = new DuplicateKeyException(
        new BsonDocument(),
        new ServerAddress("localhost"),
        WriteConcernResult.unacknowledged()
    );

    when(paymentDao.insert(any(Payment.class))).thenThrow(mongoException);
    Payment payment = new Payment();

    PaymentDuplicateException thrown = assertThrows(
        PaymentDuplicateException.class,
        () -> paymentService.createPayment(payment)
    );

    assertEquals("Payment already exists", thrown.getMessage());
    verify(paymentDao).insert(payment);
  }

  @Test
  void createPayment_ShouldHandleApiError_BySettingFailedStatus() {
    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API Down")));

    Payment payment = new Payment();
    when(paymentDao.insert(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
    Payment result = paymentService.createPayment(payment);

    assertEquals(PaymentStatus.FAILED, result.getStatus());
  }

  @Test
  void findPaymentsTotalSumForUserForDateRange_ShouldReturnCorrectSum() {
    Long userId = 1L;
    LocalDate start = LocalDate.now().minusDays(1);
    LocalDate end = LocalDate.now();

    List<Payment> mockPayments = List.of(
        new Payment("1", 101L, userId, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("50.00")),
        new Payment("2", 102L, userId, PaymentStatus.FAILED, LocalDateTime.now(), new BigDecimal("100.00")),
        new Payment("3", 103L, userId, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("25.50"))
    );

    when(paymentDao.findByUserIdAndTimestampBetween(eq(userId), any(), any())).thenReturn(mockPayments);
    BigDecimal total = paymentService.findPaymentsTotalSumForUserForDateRange(userId, start, end);

    assertEquals(0, new BigDecimal("75.50").compareTo(total));
  }

  @Test
  void getPaymentsByStatus_ShouldCallDao() {
    PaymentStatus status = PaymentStatus.SUCCESS;
    paymentService.getPaymentsByStatus(status);
    verify(paymentDao).findByStatus(status);
  }

  @Test
  void getPaymentsByOrderId_ShouldCallDao() {
    Long orderId = 55L;
    paymentService.getPaymentsByOrderId(orderId);
    verify(paymentDao).findByOrderId(orderId);
  }

  @Test
  void findPaymentsTotalSumForAllUsersForDateRange_ShouldReturnCorrectSum() {
    LocalDate start = LocalDate.now().minusDays(5);
    LocalDate end = LocalDate.now();

    List<Payment> mockPayments = List.of(
        new Payment("1", 101L, 1L, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("100.00")),
        new Payment("2", 102L, 2L, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("200.00")),
        new Payment("3", 103L, 3L, PaymentStatus.FAILED, LocalDateTime.now(), new BigDecimal("500.00"))
    );

    when(paymentDao.findAllByDateRange(any(), any())).thenReturn(mockPayments);
    BigDecimal total = paymentService.findPaymentsTotalSumForAllUsersForDateRange(start, end);

    assertEquals(0, new BigDecimal("300.00").compareTo(total));
  }

  @Test
  void findPaymentsTotalSum_ShouldIgnoreNullAmounts() {
    LocalDate start = LocalDate.now();
    LocalDate end = LocalDate.now();

    List<Payment> mockPayments = List.of(
        new Payment("1", 101L, 1L, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("100.00")),
        new Payment("2", 102L, 1L, PaymentStatus.SUCCESS, LocalDateTime.now(), null)
    );

    when(paymentDao.findAllByDateRange(any(), any())).thenReturn(mockPayments);
    BigDecimal total = paymentService.findPaymentsTotalSumForAllUsersForDateRange(start, end);

    assertEquals(0, new BigDecimal("100.00").compareTo(total));
  }

  @Test
  void getPaymentsByUserId_ShouldCallDao() {
    Long userId = 99L;
    paymentService.getPaymentsByUserId(userId);
    verify(paymentDao).findByUserId(userId);
  }

  @Test
  void findPaymentsTotalSum_ShouldReturnZero_WhenNoPaymentsFound() {
    when(paymentDao.findAllByDateRange(any(), any())).thenReturn(List.of());
    BigDecimal total = paymentService.findPaymentsTotalSumForAllUsersForDateRange(LocalDate.now(), LocalDate.now());

    assertEquals(0, BigDecimal.ZERO.compareTo(total));
  }
}
