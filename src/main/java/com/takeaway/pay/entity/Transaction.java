package com.takeaway.pay.entity;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Transaction {

    private final long customerId;
    private final long restaurantId;
    private final BigDecimal transferAmount;

    public Transaction(long customerId, long restaurantId, BigDecimal transferAmount) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.transferAmount = transferAmount;
    }

}
