package com.innowise.paymentservice.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.paymentservice.TestcontainersConfiguration;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {

  @RegisterExtension
  protected static WireMockExtension wiremock = WireMockExtension.newInstance()
      .options(options().dynamicPort())
      .build();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("app.random-api.url", wiremock::baseUrl);
  }
}
