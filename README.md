## Build:
```
mvn clean package
```

## Run:
- Run the app, default port 8080:
```
java -jar target/pay-01.00.jar
```


curl -X PUT http://localhost:8080/pay/createCustomer?id=78
curl -X PUT http://localhost:8080/pay/createRestaurant?id=3

curl -X GET http://localhost:8080/pay/getCustomerBalance?id=78
curl -X GET http://localhost:8080/pay/getRestaurantBalance?id=3

curl -X POST http://localhost:8080/pay/topup -H "Content-Type: application/json" -d '{"customerId": 78, "topUpAmount": 20.0}'

curl -X POST http://localhost:8080/pay/transfer -H "Content-Type: application/json" -d '{"customerId": 78, "restaurantId": 3, "transferAmount": 8.0}' 
