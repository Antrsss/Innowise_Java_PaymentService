package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
  Payment toPayment(PaymentDto paymentDto);
  PaymentResponseDto toPaymentResponseDto(Payment payment);
  List<PaymentResponseDto> toPaymentResponseDtoList(List<Payment> payments);
}
