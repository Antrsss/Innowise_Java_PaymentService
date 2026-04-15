package com.innowise.paymentservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
@CompoundIndex(name = "idx_user_id_timestamp", def = "{'user_id': 1, 'timestamp': -1}")
@Getter @Setter
public class Payment {

  @Id
  private Long id;

  @Field(name = "order_id")
  @Indexed(name = "idx_order_id")
  private Long orderId;

  @Field(name = "user_id")
  @Indexed(name = "idx_user_id")
  private Long userId;

  @Indexed(name = "idx_status")
  private String status;

  @Indexed(name = "idx_timestamp")
  private LocalDateTime timestamp;

  @Field(name = "payment_amount")
  private BigDecimal paymentAmount;
}
