package com.takeaway.pay.service.impl;

import com.takeaway.pay.exception.PayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryPayServiceTest {

    private InMemoryPayService payService;

    private static final long VALID_CUST_ID = 123;
    private static final long VALID_RESTAURANT_ID = 789;

    @BeforeEach
    public void testSetup() {
        payService = new InMemoryPayService();
        payService.createCustomerAllowance(VALID_CUST_ID);
        payService.topUpCustomerAllowance(VALID_CUST_ID, BigDecimal.valueOf(20.0));
        payService.createRestaurantAccount(VALID_RESTAURANT_ID);
    }

    @Test
    public void testCreateNewCustomerAllowance() {
        payService.createCustomerAllowance(111);
        assertEquals(BigDecimal.valueOf(0.0), payService.getCustomerAllowanceBalance(111), "new customer should have zero balance");
    }

    @Test
    public void testTopUpCustomerAllowance() {
        payService.createCustomerAllowance(222);
        payService.topUpCustomerAllowance(222, BigDecimal.valueOf(20.0));
        assertEquals(BigDecimal.valueOf(20.0), payService.getCustomerAllowanceBalance(222), "Customer has incorrect balance after topup");
    }

    @Test
    public void testTopUpCustomerAllowance_NonExistentCustomer() {

        assertThrows(IllegalArgumentException.class, () -> payService.topUpCustomerAllowance(333, BigDecimal.valueOf(100.0)));
    }

    @Test
    public void testCheckBalance_NonExistentCustomer() {

        assertThrows(IllegalArgumentException.class, () -> payService.getCustomerAllowanceBalance(333));
    }

    @Test
    public void testCheckBalance_NonExistentRestaurant() {

        assertThrows(IllegalArgumentException.class, () -> payService.getRestaurantAccountBalance(333));
    }

    @Test
    public void testCreateNewRestaurantAccount() {
        payService.createRestaurantAccount(1);
        assertEquals(BigDecimal.valueOf(0.0), payService.getRestaurantAccountBalance(1), "new restaurant should have zero balance");
    }

    @Test
    public void testTransfer_validFirstTransfer() throws PayException {

        assertEquals(BigDecimal.valueOf(20.0), payService.getCustomerAllowanceBalance(VALID_CUST_ID));
        payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(8.0));
        assertEquals(BigDecimal.valueOf(12.0), payService.getCustomerAllowanceBalance(VALID_CUST_ID));
        assertEquals(BigDecimal.valueOf(8.0), payService.getRestaurantAccountBalance(VALID_RESTAURANT_ID));

    }

    @Test
    public void testTransfer_tooLargeFirstTransfer() {
        Throwable exception = assertThrows(PayException.class, () -> payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(12.0)));
        assertEquals(InMemoryPayService.DAILY_LIMIT_ERROR_MSG, exception.getMessage());

    }


    @Test
    public void testTransfer_validTransfer() throws PayException {


        payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(4.0));
        assertEquals(BigDecimal.valueOf(16.0), payService.getCustomerAllowanceBalance(VALID_CUST_ID));
        assertEquals(BigDecimal.valueOf(4.0), payService.getRestaurantAccountBalance(VALID_RESTAURANT_ID));

        payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(6.0));
        assertEquals(BigDecimal.valueOf(10.0), payService.getCustomerAllowanceBalance(VALID_CUST_ID));
        assertEquals(BigDecimal.valueOf(10.0), payService.getRestaurantAccountBalance(VALID_RESTAURANT_ID));

    }

    @Test
    public void testTransfer_insufficientBalance() throws PayException {

        long poorCust = 777;

        payService.createCustomerAllowance(poorCust);
        payService.topUpCustomerAllowance(poorCust, BigDecimal.valueOf(5.0)); //topup only 5 EUR
        payService.createRestaurantAccount(VALID_RESTAURANT_ID);

        payService.transfer(poorCust, VALID_RESTAURANT_ID, BigDecimal.valueOf(4.0));
        assertEquals(BigDecimal.valueOf(1.0), payService.getCustomerAllowanceBalance(poorCust));
        assertEquals(BigDecimal.valueOf(4.0), payService.getRestaurantAccountBalance(VALID_RESTAURANT_ID));

        Throwable exception = assertThrows(PayException.class, () -> payService.transfer(poorCust, VALID_RESTAURANT_ID, BigDecimal.valueOf(2.0)));
        assertEquals(InMemoryPayService.INSUFICUENT_FUNDS_ERROR_MSG, exception.getMessage());

    }

    @Test
    public void testTransfer_dailyLimitBreach() throws PayException {

        payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(4.0));
        assertEquals(BigDecimal.valueOf(16.0), payService.getCustomerAllowanceBalance(VALID_CUST_ID));
        assertEquals(BigDecimal.valueOf(4.0), payService.getRestaurantAccountBalance(VALID_RESTAURANT_ID));

        payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(6.0));
        assertEquals(BigDecimal.valueOf(10.0), payService.getCustomerAllowanceBalance(VALID_CUST_ID));
        assertEquals(BigDecimal.valueOf(10.0), payService.getRestaurantAccountBalance(VALID_RESTAURANT_ID));

        Throwable exception = assertThrows(PayException.class, () -> payService.transfer(VALID_CUST_ID, VALID_RESTAURANT_ID, BigDecimal.valueOf(2.0)));
        assertEquals(InMemoryPayService.DAILY_LIMIT_ERROR_MSG, exception.getMessage());

    }
}
