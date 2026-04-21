package com.innowise.paymentservice.integration;

import com.innowise.paymentservice.TestcontainersConfiguration;
import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
class PaymentIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private PaymentDao paymentDao;

  @BeforeEach
  void cleanUp() {
    paymentDao.deleteAll();
  }

  @Test
  void createPayment_Success_WhenApiReturnsEvenNumber() {
    wiremock.stubFor(get(urlMatching("/integers/.*"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "text/plain")
            .withBody("4")));

    PaymentDto request = new PaymentDto(1L, 1L, new BigDecimal("100.00"));

    webTestClient.post()
        .uri("/api/payments")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(PaymentResponseDto.class)
        .value(response -> {
          assertNotNull(response.id());
          assertEquals(PaymentStatus.SUCCESS, response.status());
          assertEquals(new BigDecimal("100.00"), response.paymentAmount());
        });

    Long count = paymentDao.count();
    assertEquals(1, count);
  }

  @Test
  void createPayment_Failed_WhenApiReturnsOddNumber() {
    wiremock.stubFor(get(urlMatching("/integers/.*"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "text/plain")
            .withBody("7")));

    PaymentDto request = new PaymentDto(2L, 1L, new BigDecimal("50.00"));

    webTestClient.post()
        .uri("/api/payments")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.status").isEqualTo("FAILED");
  }

  @Test
  void getPaymentsByUserId_ShouldReturnList() {
    webTestClient.get()
        .uri("/api/payments/user/{userId}", 1L)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(PaymentResponseDto.class);
  }
}