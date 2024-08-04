package com.takeaway.pay.controller;

import com.takeaway.pay.entity.TopUp;
import com.takeaway.pay.entity.Transaction;
import com.takeaway.pay.service.PayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PayControllerTest {

	@Value(value="${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void integrationTest() throws Exception {

        String url = "http://localhost:"+ port +"/pay/";
        long customerId = 123;
        long restaurantId = 234;

        URI createCustomerUri = UriComponentsBuilder.fromHttpUrl(url).path("createCustomer").queryParam("id", customerId).build().toUri();
        this.restTemplate.put(createCustomerUri, null);

        URI createRestaurantUri = UriComponentsBuilder.fromHttpUrl(url).path("createRestaurant").queryParam("id", restaurantId).build().toUri();
        this.restTemplate.put(createRestaurantUri, null);

        URI getCustomerBalanceUri = UriComponentsBuilder.fromHttpUrl(url).path("getCustomerBalance").queryParam("id", customerId).build().toUri();
        String custBalance = this.restTemplate.getForObject(getCustomerBalanceUri, String.class);
        assertEquals("0.0", custBalance);

        URI getRestaurantBalanceUri = UriComponentsBuilder.fromHttpUrl(url).path("getRestaurantBalance").queryParam("id", restaurantId).build().toUri();
        String restBalance = this.restTemplate.getForObject(getRestaurantBalanceUri, String.class);
        assertEquals("0.0", restBalance);

        TopUp topUp = new TopUp(customerId, BigDecimal.valueOf(20.0));

        URI topUpUri = UriComponentsBuilder.fromHttpUrl(url).path("topup").build().toUri();
        ResponseEntity<String> topUpStatus = this.restTemplate.postForEntity(topUpUri, topUp, String.class);

        assertEquals(PayController.TOPUP_SUCCESS, topUpStatus.getBody());

        custBalance = this.restTemplate.getForObject(getCustomerBalanceUri, String.class);
        assertEquals("20.0", custBalance);

        Transaction transaction = new Transaction(customerId, restaurantId, BigDecimal.valueOf(8.0));

        URI transferUri = UriComponentsBuilder.fromHttpUrl(url).path("transfer").build().toUri();
        ResponseEntity<String> transferStatus = this.restTemplate.postForEntity(transferUri, transaction, String.class);

        assertEquals(PayController.TRANSFER_SUCCESS, transferStatus.getBody());

        custBalance = this.restTemplate.getForObject(getCustomerBalanceUri, String.class);
        assertEquals("12.0", custBalance);

        restBalance = this.restTemplate.getForObject(getRestaurantBalanceUri, String.class);
        assertEquals("8.0", restBalance);
        
	}

    @Test
	public void integrationTest_daliyLimitBreach() throws Exception {

        String url = "http://localhost:"+ port +"/pay/";
        long customerId = 888;
        long restaurantId = 999;

        URI createCustomerUri = UriComponentsBuilder.fromHttpUrl(url).path("createCustomer").queryParam("id", customerId).build().toUri();
        this.restTemplate.put(createCustomerUri, null);

        URI createRestaurantUri = UriComponentsBuilder.fromHttpUrl(url).path("createRestaurant").queryParam("id", restaurantId).build().toUri();
        this.restTemplate.put(createRestaurantUri, null);

        URI getCustomerBalanceUri = UriComponentsBuilder.fromHttpUrl(url).path("getCustomerBalance").queryParam("id", customerId).build().toUri();
        String custBalance = this.restTemplate.getForObject(getCustomerBalanceUri, String.class);
        assertEquals("0.0", custBalance);

        URI getRestaurantBalanceUri = UriComponentsBuilder.fromHttpUrl(url).path("getRestaurantBalance").queryParam("id", restaurantId).build().toUri();
        String restBalance = this.restTemplate.getForObject(getRestaurantBalanceUri, String.class);
        assertEquals("0.0", restBalance);

        TopUp topUp = new TopUp(customerId, BigDecimal.valueOf(20.0));

        URI topUpUri = UriComponentsBuilder.fromHttpUrl(url).path("topup").build().toUri();
        ResponseEntity<String> topUpStatus = this.restTemplate.postForEntity(topUpUri, topUp, String.class);

        assertEquals(PayController.TOPUP_SUCCESS, topUpStatus.getBody());

        custBalance = this.restTemplate.getForObject(getCustomerBalanceUri, String.class);
        assertEquals("20.0", custBalance);

        Transaction transaction = new Transaction(customerId, restaurantId, BigDecimal.valueOf(8.2));

        URI transferUri = UriComponentsBuilder.fromHttpUrl(url).path("transfer").build().toUri();
        ResponseEntity<String> transferStatus = this.restTemplate.postForEntity(transferUri, transaction, String.class);

        assertEquals(PayController.TRANSFER_SUCCESS, transferStatus.getBody());

        custBalance = this.restTemplate.getForObject(getCustomerBalanceUri, String.class);
        assertEquals("11.8", custBalance);

        restBalance = this.restTemplate.getForObject(getRestaurantBalanceUri, String.class);
        assertEquals("8.2", restBalance);

        Transaction transaction2 = new Transaction(customerId, restaurantId, BigDecimal.valueOf(8.0));

        ResponseEntity<String> transferStatus2 = this.restTemplate.postForEntity(transferUri, transaction2, String.class);
        
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, transferStatus2.getStatusCode());
        assertEquals(PayService.DAILY_LIMIT_ERROR_MSG, transferStatus2.getBody());
        
	}

    @Test
	public void integrationTest_badInput() {

        String url = "http://localhost:"+ port +"/pay/";
        

        URI createCustomerUri = UriComponentsBuilder.fromHttpUrl(url).path("createCustomer").queryParam("id", "abc").build().toUri();
        this.restTemplate.put(createCustomerUri, null);


        URI getCustomerBalanceUri = UriComponentsBuilder.fromHttpUrl(url).path("getCustomerBalance").queryParam("id", 999).build().toUri();
        ResponseEntity<String> response = this.restTemplate.getForEntity(getCustomerBalanceUri, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

	}

    
}
