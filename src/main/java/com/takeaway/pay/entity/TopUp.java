package com.takeaway.pay.entity;

import java.math.BigDecimal;

import lombok.Getter;

public class TopUp {

    @Getter
    private final long customerId;
    @Getter
    private final BigDecimal topUpAmount;

    public TopUp(long customerId, BigDecimal topUpAmount) {
        this.customerId = customerId;
        this.topUpAmount = topUpAmount;
    }
    
}
