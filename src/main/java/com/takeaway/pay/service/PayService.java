package com.takeaway.pay.service;

import java.math.BigDecimal;

import com.takeaway.pay.exception.PayException;

public interface PayService {

    String INSUFICUENT_FUNDS_ERROR_MSG = "Insufficient fund!. Transfer amount is greater than the balance. \n\n";
    String DAILY_LIMIT_ERROR_MSG = "Transfer cannot be made as it crosses Daily Limit \n\n";
    
    
    void createCustomerAllowance(long customerId);
    void topUpCustomerAllowance(long customerId, BigDecimal topUpAmount);
    BigDecimal getCustomerAllowanceBalance(long customerId);

    void createRestaurantAccount(long restaurantId);
    BigDecimal getRestaurantAccountBalance(long restaurantId);

    boolean transfer(long customerId, long restaurantId, BigDecimal transferAmount) throws PayException;
}
