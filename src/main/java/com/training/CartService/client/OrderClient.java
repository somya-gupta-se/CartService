package com.training.CartService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "OrderService", path="/order")
public interface OrderClient {
    @PostMapping("/place")
    public void placeOrder(
            @RequestParam Long customerId,
            @RequestParam String productId,
            @RequestParam int quantity,
            @RequestParam BigDecimal totalPrice);
}
