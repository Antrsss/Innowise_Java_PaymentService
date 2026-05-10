package com.innowise.paymentservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.random-api")
public class RandomApiProperties {
  private String url;
  private String path;
  private Map<String, String> defaultParams;
}