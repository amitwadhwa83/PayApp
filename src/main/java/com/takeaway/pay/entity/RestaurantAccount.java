package com.takeaway.pay.entity;

import java.math.BigDecimal;

import lombok.Getter;

public class RestaurantAccount {

    @Getter
    private final long restaurantId;
    @Getter
    private BigDecimal balance = BigDecimal.valueOf(0.0);

    public RestaurantAccount(long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void receiveAmount(BigDecimal amount) {
        if(amount == null || BigDecimal.ZERO.compareTo(amount) == 1) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        balance = balance.add(amount);
    }
    
}
