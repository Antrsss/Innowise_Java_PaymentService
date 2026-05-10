package com.innowise.paymentservice.service;

import com.innowise.paymentservice.config.RandomApiProperties;
import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentDuplicateException;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import com.mongodb.DuplicateKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock private PaymentDao paymentDao;
  @Mock private PaymentProducer paymentProducer;
  @Mock private WebClient randomNumberWebClient;
  @Mock private RandomApiProperties apiProperties;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
  @Mock private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private PaymentServiceImpl paymentService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    lenient().when(apiProperties.getPath()).thenReturn("/integers/");
    lenient().when(apiProperties.getDefaultParams()).thenReturn(Map.of("num", "1"));

    lenient().when(randomNumberWebClient.get()).thenReturn(requestHeadersUriSpec);
    lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
    lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void shouldSetSuccessStatus_WhenRandomNumberIsEven() {
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("2"));
    when(paymentDao.insert(any(Payment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

    Payment payment = new Payment();
    payment.setOrderId(100L);

    StepVerifier.create(paymentService.createPayment(payment))
        .assertNext(result -> {
          assert result.getStatus() == PaymentStatus.SUCCESS;
          verify(paymentProducer).sendPaymentEvent(100L, PaymentStatus.SUCCESS);
        })
        .verifyComplete();
  }

  @Test
  void shouldSetFailedStatus_WhenRandomNumberIsOdd() {
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("1"));
    when(paymentDao.insert(any(Payment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

    Payment payment = new Payment();
    payment.setOrderId(1L);

    StepVerifier.create(paymentService.createPayment(payment))
        .assertNext(result -> {
          assert result.getStatus() == PaymentStatus.FAILED;
          verify(paymentProducer).sendPaymentEvent(1L, PaymentStatus.FAILED);
        })
        .verifyComplete();
  }

  @Test
  void createPayment_ShouldThrowDuplicateException_WhenPaymentAlreadyExists() {
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("4"));
    when(paymentDao.insert(any(Payment.class)))
        .thenReturn(Mono.error(mock(DuplicateKeyException.class)));

    StepVerifier.create(paymentService.createPayment(new Payment()))
        .expectError(PaymentDuplicateException.class)
        .verify();
  }

  @Test
  void findPaymentsTotalSumForUserForDateRange_ShouldReturnCorrectSum() {
    Long userId = 1L;
    Flux<Payment> mockPayments = Flux.just(
        new Payment("1", 101L, userId, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("50.00")),
        new Payment("2", 102L, userId, PaymentStatus.FAILED, LocalDateTime.now(), new BigDecimal("100.00")),
        new Payment("3", 103L, userId, PaymentStatus.SUCCESS, LocalDateTime.now(), new BigDecimal("25.50"))
    );

    when(paymentDao.findByUserIdAndTimestampBetween(eq(userId), any(), any())).thenReturn(mockPayments);

    StepVerifier.create(paymentService.findPaymentsTotalSumForUserForDateRange(userId, LocalDate.now(), LocalDate.now()))
        .expectNextMatches(total -> total.compareTo(new BigDecimal("75.50")) == 0)
        .verifyComplete();
  }

  @Test
  void getPaymentsByStatus_ShouldCallDao() {
    when(paymentDao.findByStatus(PaymentStatus.SUCCESS)).thenReturn(Flux.empty());

    StepVerifier.create(paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS))
        .verifyComplete();

    verify(paymentDao).findByStatus(PaymentStatus.SUCCESS);
  }

  @Test
  void findPaymentsTotalSum_ShouldReturnZero_WhenNoPaymentsFound() {
    when(paymentDao.findAllByDateRange(any(), any())).thenReturn(Flux.empty());

    StepVerifier.create(paymentService.findPaymentsTotalSumForAllUsersForDateRange(LocalDate.now(), LocalDate.now()))
        .expectNext(BigDecimal.ZERO)
        .verifyComplete();
  }
}