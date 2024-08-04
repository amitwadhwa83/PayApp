package com.takeaway.pay.entity;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TopUp {

    private final long customerId;
    private final BigDecimal topUpAmount;

    public TopUp(long customerId, BigDecimal topUpAmount) {
        this.customerId = customerId;
        this.topUpAmount = topUpAmount;
    }

}
