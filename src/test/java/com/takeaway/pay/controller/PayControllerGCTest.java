package com.takeaway.pay.controller;

import com.takeaway.pay.entity.TopUp;
import com.takeaway.pay.entity.Transaction;
import com.takeaway.pay.exception.PayException;
import com.takeaway.pay.service.PayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PayControllerGCTest {

    @Mock
    private PayService payService;

    @InjectMocks
    private PayController payController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateCustomerAllowance() {
        String response = payController.createCustomerAllowance(123L);
        assertEquals(String.format(PayController.CREATE_CUSTOMER_SUCCESS, 123L), response);
        verify(payService, times(1)).createCustomerAllowance(123L);
    }

    @Test
    public void testCreateRestaurantAccount() {
        String response = payController.createRestaurantAccount(456L);
        assertEquals(String.format(PayController.CREATE_RESTAURANT_SUCCESS, 456L), response);
        verify(payService, times(1)).createRestaurantAccount(456L);
    }

    @Test
    public void testGetCustomerAllowanceBalance() {
        when(payService.getCustomerAllowanceBalance(anyLong())).thenReturn(BigDecimal.valueOf(20.0));
        ResponseEntity<String> response = payController.getCustomerAllowanceBalance(123L);
        assertEquals(ResponseEntity.ok().body("20.0"), response);
    }

    @Test
    public void testGetCustomerAllowanceBalance_NonExistentCustomer() {
        when(payService.getCustomerAllowanceBalance(anyLong())).thenThrow(new IllegalArgumentException("Customer not found"));
        ResponseEntity<String> response = payController.getCustomerAllowanceBalance(123L);
        assertEquals(ResponseEntity.badRequest().body("Customer not found\n\n"), response);
    }

    @Test
    public void testGetRestaurantAccountBalance() {
        when(payService.getRestaurantAccountBalance(anyLong())).thenReturn(BigDecimal.valueOf(30.0));
        ResponseEntity<String> response = payController.getRestaurantAccountBalance(456L);
        assertEquals(ResponseEntity.ok().body("30.0"), response);
    }

    @Test
    public void testGetRestaurantAccountBalance_NonExistentRestaurant() {
        when(payService.getRestaurantAccountBalance(anyLong())).thenThrow(new IllegalArgumentException("Restaurant not found"));
        ResponseEntity<String> response = payController.getRestaurantAccountBalance(456L);
        assertEquals(ResponseEntity.badRequest().body("Restaurant not found\n\n"), response);
    }

    @Test
    public void testTransfer() throws PayException {
        doNothing().when(payService).transfer(anyLong(), anyLong(), any(BigDecimal.class));
        Transaction transaction = new Transaction(123L, 456L, BigDecimal.valueOf(10.0));
        ResponseEntity<String> response = payController.transfer(transaction);
        assertEquals(ResponseEntity.ok().body(PayController.TRANSFER_SUCCESS), response);
    }

    @Test
    public void testTransfer_PayException() throws PayException {
        doThrow(new PayException("Daily limit exceeded")).when(payService).transfer(anyLong(), anyLong(), any(BigDecimal.class));
        Transaction transaction = new Transaction(123L, 456L, BigDecimal.valueOf(10.0));
        ResponseEntity<String> response = payController.transfer(transaction);
        assertEquals(ResponseEntity.unprocessableEntity().body("Daily limit exceeded"), response);
    }

    @Test
    public void testTopup() {
        doNothing().when(payService).topUpCustomerAllowance(anyLong(), any(BigDecimal.class));
        TopUp topUp = new TopUp(123L, BigDecimal.valueOf(20.0));
        ResponseEntity<String> response = payController.topup(topUp);
        assertEquals(ResponseEntity.ok().body(PayController.TOPUP_SUCCESS), response);
    }

    @Test
    public void testTopup_IllegalArgumentException() {
        doThrow(new IllegalArgumentException("Invalid top-up amount")).when(payService).topUpCustomerAllowance(anyLong(), any(BigDecimal.class));
        TopUp topUp = new TopUp(123L, BigDecimal.valueOf(20.0));
        ResponseEntity<String> response = payController.topup(topUp);
        assertEquals(ResponseEntity.badRequest().body("Invalid top-up amount\n\n"), response);
    }
}
