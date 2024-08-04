package com.takeaway.pay.controller;


import com.takeaway.pay.entity.TopUp;
import com.takeaway.pay.entity.Transaction;
import com.takeaway.pay.exception.PayException;
import com.takeaway.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/pay/")
public class PayController {

    @Autowired
    private PayService payService;

    public static final String CREATE_CUSTOMER_SUCCESS = "Customer with id %s is created successfully \n\n";
    public static final String CREATE_RESTAURANT_SUCCESS = "Restaurant with id %s is created successfully \n\n";

    public static final String TRANSFER_SUCCESS = "Transfer was successful\n\n";
    public static final String TOPUP_SUCCESS = "TopUp was successful\n\n";

    @PutMapping("createCustomer")
    public String createCustomerAllowance(@RequestParam long id) {
        payService.createCustomerAllowance(id);
        return String.format(CREATE_CUSTOMER_SUCCESS, id);
    }

    @PutMapping("createRestaurant")
    public String createRestaurantAccount(@RequestParam long id) {
        payService.createRestaurantAccount(id);
        return String.format(CREATE_RESTAURANT_SUCCESS, id);
    }

    @GetMapping("getCustomerBalance")
    @ResponseBody
    public ResponseEntity<String> getCustomerAllowanceBalance(@RequestParam long id) {
        try {
            BigDecimal balance = payService.getCustomerAllowanceBalance(id);
            return ResponseEntity.ok().body(balance.toString());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException.getMessage() + "\n\n");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage() + "\n\n");
        }
    }

    @GetMapping("getRestaurantBalance")
    @ResponseBody
    public ResponseEntity<String> getRestaurantAccountBalance(@RequestParam long id) {
        try {
            BigDecimal balance = payService.getRestaurantAccountBalance(id);
            return ResponseEntity.ok().body(balance.toString());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException.getMessage() + "\n\n");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage() + "\n\n");
        }
    }

    @PostMapping(value = "transfer")
    public ResponseEntity<String> transfer(@RequestBody Transaction transaction) {
        try {
            payService.transfer(transaction.getCustomerId(), transaction.getRestaurantId(), transaction.getTransferAmount());
            return ResponseEntity.ok().body(TRANSFER_SUCCESS);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException.getMessage() + "\n\n");
        } catch (PayException payException) {
            return ResponseEntity.unprocessableEntity().body(payException.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage() + "\n\n");
        }

    }

    @PostMapping(value = "topup")
    public ResponseEntity<String> topup(@RequestBody TopUp topUp) {
        try {
            payService.topUpCustomerAllowance(topUp.getCustomerId(), topUp.getTopUpAmount());
            return ResponseEntity.ok().body(TOPUP_SUCCESS);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException.getMessage() + "\n\n");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage() + "\n\n");
        }

    }


}