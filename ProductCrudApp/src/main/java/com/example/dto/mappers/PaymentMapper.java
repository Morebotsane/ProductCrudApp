package com.example.dto.mappers;

import com.example.dto.PaymentDTO;
import com.example.entities.Payment;

import java.util.List;
import java.util.stream.Collectors;

public class PaymentMapper {

    public static PaymentDTO toDto(Payment payment) {
        if (payment == null) return null;

        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        dto.setMethod(payment.getMethod());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setTxnRef(payment.getTxnRef());
        dto.setCreatedAt(payment.getCreatedAt());

        return dto;
    }

    public static List<PaymentDTO> toDto(List<Payment> payments) {
        return payments != null
                ? payments.stream().map(PaymentMapper::toDto).collect(Collectors.toList())
                : null;
    }
}
