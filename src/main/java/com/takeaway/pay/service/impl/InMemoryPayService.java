package com.takeaway.pay.service.impl;

import com.takeaway.pay.entity.CustomerAllowance;
import com.takeaway.pay.entity.RestaurantAccount;
import com.takeaway.pay.exception.PayException;
import com.takeaway.pay.service.PayService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class InMemoryPayService implements PayService {

    private static final BigDecimal DAILY_LIMIT_IN_EUR = BigDecimal.valueOf(10.0);

    private final ConcurrentMap<Long, CustomerAllowance> customerAllowances = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, RestaurantAccount> restaurantAccounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, BigDecimal> dailyAmountTransferredPerCustomer = new ConcurrentHashMap<>();


    @Override
    public void createCustomerAllowance(long customerId) {
        customerAllowances.putIfAbsent(customerId, new CustomerAllowance(customerId));
    }

    @Override
    public void topUpCustomerAllowance(long customerId, BigDecimal topUpAmount) {
        CustomerAllowance customerAllowance = customerAllowances.get(customerId);
        if (customerAllowance != null) {
            synchronized (customerAllowance) {
                customerAllowances.get(customerId).topUp(topUpAmount);
            }
        } else {
            throw new IllegalArgumentException("Customer with Id : " + customerId + " does not exist!");
        }
    }

    @Override
    public BigDecimal getCustomerAllowanceBalance(long customerId) {
        CustomerAllowance customerAllowance = customerAllowances.get(customerId);
        if (customerAllowance != null) {
            synchronized (customerAllowance) {
                return customerAllowance.getBalance();
            }
        } else {
            throw new IllegalArgumentException("Customer with Id : " + customerId + " does not exist!");
        }
    }

    @Override
    public void createRestaurantAccount(long restaurantId) {
        restaurantAccounts.putIfAbsent(restaurantId, new RestaurantAccount(restaurantId));
    }

    @Override
    public BigDecimal getRestaurantAccountBalance(long restaurantId) {
        RestaurantAccount restaurantAccount = restaurantAccounts.get(restaurantId);
        if (restaurantAccount != null) {
            synchronized (restaurantAccount) {
                return restaurantAccount.getBalance();
            }
        } else {
            throw new IllegalArgumentException("Restaurant with id : " + restaurantId + " does not exist!");
        }
    }

    //Not exposed directly to the world.
    protected boolean transfer(long customerId, long restaurantId, BigDecimal transferAmount, LocalDate transferDate) throws PayException {

        String errorMsgs = transferPreChecks(customerId, restaurantId, transferAmount, transferDate);
        if (!errorMsgs.isBlank()) {
            throw new IllegalArgumentException(errorMsgs);
        }

        CustomerAllowance customerAllowance = customerAllowances.get(customerId);
        RestaurantAccount restaurantAccount = restaurantAccounts.get(restaurantId);

        synchronized (customerAllowance) {
            synchronized (restaurantAccount) {

                checkSufficientBalance(customerAllowance, transferAmount);
                checkDailyLimit(customerAllowance, transferAmount, transferDate);

                LocalDate lastDeductionDate = customerAllowance.getLastDeductionDate();

                try {
                    customerAllowance.deduct(transferAmount, transferDate);
                    restaurantAccount.receiveAmount(transferAmount);

                    if (lastDeductionDate != null && transferDate.isEqual(lastDeductionDate)) {
                        //Same day transaction but within limit
                        dailyAmountTransferredPerCustomer.computeIfPresent(customerId, (custId, totalTransferAmount) -> totalTransferAmount.add(transferAmount));
                    } else {
                        //first transaction or a new day transaction
                        dailyAmountTransferredPerCustomer.put(customerId, transferAmount);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Rolling back Transfer");
                    //TODO: add logic to rollback, for now, in this simple case this should not occur
                    return false;
                }
                return true;

            }
        }

    }

    private void checkDailyLimit(CustomerAllowance customerAllowance, BigDecimal transferAmount, LocalDate transferDate) throws PayException {
        if (transferAmount.compareTo(DAILY_LIMIT_IN_EUR) > 0) {
            throw new PayException(DAILY_LIMIT_ERROR_MSG);
        }
        LocalDate lastDeductionDate = customerAllowance.getLastDeductionDate();
        if (lastDeductionDate != null && transferDate.isEqual(lastDeductionDate)) {
            BigDecimal totalTransferAmount = dailyAmountTransferredPerCustomer.get(customerAllowance.getCustomerId());
            if (totalTransferAmount != null) {

                totalTransferAmount = totalTransferAmount.add(transferAmount);
                if (totalTransferAmount.compareTo(DAILY_LIMIT_IN_EUR) > 0) {
                    throw new PayException(DAILY_LIMIT_ERROR_MSG);
                }

            } else {//Should not happen
                throw new IllegalStateException("If lastDeductionDate is NOT Null then totalTransferAmout for that date cannot be NULL");
            }
        }

    }

    private void checkSufficientBalance(CustomerAllowance customerAllowance, BigDecimal transferAmount) throws PayException {
        BigDecimal currentBalance = customerAllowance.getBalance();
        if (currentBalance.compareTo(transferAmount) < 0) {
            throw new PayException(INSUFICUENT_FUNDS_ERROR_MSG);
        }
    }

    private String transferPreChecks(long customerId, long restaurantId, BigDecimal transferAmount,
                                     LocalDate transferDate) {
        StringBuilder errorMsgs = new StringBuilder();
        if (transferDate == null) {
            errorMsgs.append("TransferDate cannot be null.");
        }
        if (!customerAllowances.containsKey(customerId)) {
            errorMsgs.append("Customer with Id : " + customerId + " does not exist!");
        }
        if (!restaurantAccounts.containsKey(restaurantId)) {
            errorMsgs.append("Restaurant with id : ").append(restaurantId).append(" does not exist!");
        }
        if (transferAmount == null || BigDecimal.ZERO.compareTo(transferAmount) == 1) {
            errorMsgs.append("Transfer amount cannot be negative");
        }
        return errorMsgs.toString();
    }

    @Override
    public void transfer(long customerId, long restaurantId, BigDecimal transferAmount) throws PayException {
        transfer(customerId, restaurantId, transferAmount, LocalDate.now());
    }
}
