package com.takeaway.pay.entity;

import java.math.BigDecimal;

import lombok.Getter;

public class Transaction {

    @Getter
    private final long customerId;
    @Getter
    private final long restaurantId;
    @Getter
    private final BigDecimal transferAmount;

    public Transaction(long customerId, long restaurantId, BigDecimal transferAmount) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.transferAmount = transferAmount;
    }
    
}
