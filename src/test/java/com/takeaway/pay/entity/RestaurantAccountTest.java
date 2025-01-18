package com.takeaway.pay.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantAccountTest {

    private RestaurantAccount restaurantAccount;

    @BeforeEach
    public void setUp() {
        restaurantAccount = new RestaurantAccount(789L);
    }

    @Test
    public void testReceiveAmount() {
        restaurantAccount.receiveAmount(BigDecimal.valueOf(50.0));
        assertEquals(BigDecimal.valueOf(50.0), restaurantAccount.getBalance());
    }

    @Test
    public void testReceiveAmount_NegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> restaurantAccount.receiveAmount(BigDecimal.valueOf(-10.0)));
    }

    @Test
    public void testReceiveAmount_NullAmount() {
        assertThrows(IllegalArgumentException.class, () -> restaurantAccount.receiveAmount(null));
    }
}