package com.innowise.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RandomNumberConfig {

  @Bean
  public WebClient randomNumberWebClient(RandomApiProperties properties) {
    return WebClient.builder()
        .baseUrl(properties.getUrl())
        .build();
  }
}