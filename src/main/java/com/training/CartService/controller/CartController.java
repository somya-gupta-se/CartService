package com.training.CartService.controller;

import com.training.CartService.enitity.Cart;
import com.training.CartService.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<Cart> addCart(@PathVariable Long userId,
                                          @RequestParam String productCode,
                                          @RequestParam Integer quantity,
                                          @RequestParam BigDecimal price) {
        return ResponseEntity.ok(cartService.addCart(userId, productCode, quantity, price));
    }

    @PutMapping("/modifyCart/{userId}")
    public ResponseEntity<Cart> modifyCart(@PathVariable Long userId,
                                        @RequestParam String productCode,
                                        @RequestParam Integer quantity,
                                        @RequestParam BigDecimal price) {
        return ResponseEntity.ok(cartService.modifyCart(userId, productCode, quantity, price));
    }



    @DeleteMapping("/remove/{userId}")
    public void removeCart(@PathVariable Long userId) {
        cartService.removeCart(userId);
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkout(@PathVariable Long userId) {
        cartService.checkout(userId);
        return ResponseEntity.ok("Checkout successful");
    }
}
