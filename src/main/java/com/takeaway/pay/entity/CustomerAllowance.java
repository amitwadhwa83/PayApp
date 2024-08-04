package com.takeaway.pay.entity;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class CustomerAllowance {

    private final long customerId;
    private BigDecimal balance = BigDecimal.valueOf(0.0);
    private LocalDate lastDeductionDate = null;

    public CustomerAllowance(long customerId) {
        this.customerId = customerId;
    }

    public void topUp(BigDecimal topUpAmount) {

        if (topUpAmount == null || BigDecimal.ZERO.compareTo(topUpAmount) == 1) {
            throw new IllegalArgumentException("Topup amount cannot be negative");
        }
        balance = balance.add(topUpAmount);
    }

    public void deduct(BigDecimal deductionAmount, LocalDate lastDeductionDate) {

        if (deductionAmount == null || BigDecimal.ZERO.compareTo(deductionAmount) == 1) {
            throw new IllegalArgumentException("Deduction amount cannot be negative");
        }
        if (lastDeductionDate == null) {
            lastDeductionDate = LocalDate.now();
        }
        balance = balance.subtract(deductionAmount);
        this.lastDeductionDate = lastDeductionDate;
    }

}
