package com.example.dto;

import com.example.entities.PaymentMethod;

import java.math.BigDecimal;

public class PaymentRequest {
    private BigDecimal amount;
    private PaymentMethod method;
    private String txnRef;

    // --- getters & setters ---
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getTxnRef() {
        return txnRef;
    }
    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }
}
