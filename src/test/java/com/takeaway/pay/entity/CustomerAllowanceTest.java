package com.takeaway.pay.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAllowanceTest {

    private CustomerAllowance customerAllowance;

    @BeforeEach
    public void setUp() {
        customerAllowance = new CustomerAllowance(123L);
    }

    @Test
    public void testTopUp() {
        customerAllowance.topUp(BigDecimal.valueOf(20.0));
        assertEquals(BigDecimal.valueOf(20.0), customerAllowance.getBalance());
    }

    @Test
    public void testTopUp_NegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> customerAllowance.topUp(BigDecimal.valueOf(-10.0)));
    }

    @Test
    public void testTopUp_NullAmount() {
        assertThrows(IllegalArgumentException.class, () -> customerAllowance.topUp(null));
    }

    @Test
    public void testDeduct() {
        customerAllowance.topUp(BigDecimal.valueOf(20.0));
        customerAllowance.deduct(BigDecimal.valueOf(5.0), LocalDate.of(2023, 10, 1));
        assertEquals(BigDecimal.valueOf(15.0), customerAllowance.getBalance());
        assertEquals(LocalDate.of(2023, 10, 1), customerAllowance.getLastDeductionDate());
    }

    @Test
    public void testDeduct_NegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> customerAllowance.deduct(BigDecimal.valueOf(-5.0), LocalDate.of(2023, 10, 1)));
    }

    @Test
    public void testDeduct_NullAmount() {
        assertThrows(IllegalArgumentException.class, () -> customerAllowance.deduct(null, LocalDate.of(2023, 10, 1)));
    }

    @Test
    public void testDeduct_NullDate() {
        customerAllowance.topUp(BigDecimal.valueOf(20.0));
        customerAllowance.deduct(BigDecimal.valueOf(5.0), null);
        assertEquals(BigDecimal.valueOf(15.0), customerAllowance.getBalance());
        assertEquals(LocalDate.now(), customerAllowance.getLastDeductionDate());
    }
}