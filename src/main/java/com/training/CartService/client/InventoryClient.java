package com.training.CartService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "InventoryService", path = "/inventory")
public interface InventoryClient {

    @GetMapping("/check/{productCode}/in-stock")
    Boolean checkStock(@PathVariable String productCode);
}
