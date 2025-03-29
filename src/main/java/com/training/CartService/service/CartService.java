package com.training.CartService.service;

import com.training.CartService.client.InventoryClient;
import com.training.CartService.client.OrderClient;
import com.training.CartService.enitity.Cart;
import com.training.CartService.enitity.CartItem;
import com.training.CartService.repository.CartItemRepository;
import com.training.CartService.repository.CartRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private OrderClient orderClient;

    Logger LOGGER = LoggerFactory.getLogger(Cart.class);

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setTotalPrice(BigDecimal.ZERO);
            LOGGER.info("cart retrieved with details :"+Objects.nonNull(newCart));
            return cartRepository.save(newCart);
        });
    }

    public Cart addCart(Long userId, String productCode, Integer quantity, BigDecimal price) {
        Cart cart = getCartByUserId(userId);
        cart.setProductCode(productCode);
        cart.setQuantity(quantity);
        cart.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
        LOGGER.info("cart retrieved with details :"+Objects.nonNull(cart));
        return cartRepository.save(cart);
    }

    public Cart modifyCart(Long userId, String productCode, Integer quantity, BigDecimal price) {

        Cart cart = getCartByUserId(userId);
        LOGGER.info("cart details before adding product to cart:"+ (Objects.nonNull(cart)?cart:""));
        if(!Objects.equals(cart.getProductCode(), productCode)){
            LOGGER.info("For now only same product can be added to cart means qty can be increased " +
                    "or decreased for already added product, new product can not be added to cart as " +
                    "cart has only one product not list of products");
            return cart;
        }
        cart.setQuantity(cart.getQuantity()+quantity);
        cart.setTotalPrice(cart.getTotalPrice().add(price.multiply(BigDecimal.valueOf(quantity))));
        LOGGER.info("product added to cart now cart details :"+(Objects.nonNull(cart)?cart:""));
        return cartRepository.save(cart);
    }

    /*public Cart removeFromCart(Long userId, String productCode) {
        Cart cart = getCartByUserId(userId);

        //cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return cartRepository.save(cart);
    }*/

    public void checkout(Long userId) {
        Cart cart = getCartByUserId(userId);
        /* TODO::
        *   need to check the stock of product in cart so retry mechanism to
        * check stock in inventory
        * Then circuit breaker and feign to hit place order of order service */
        if(checkCartStock(cart)){
            System.out.println("Checkout completed for User ID: " + userId);
            placeOrder(cart);
            cartRepository.delete(cart);
        }
        else {
            System.out.println("Stocks not enough so unable to checkout");
        }
    }

    private void placeOrder(Cart cart) {
        orderClient.placeOrder(cart.getUserId(), cart.getProductCode(), cart.getQuantity(), cart.getTotalPrice());
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "checkStockFallback")
    public boolean checkCartStock(Cart cart) {
        boolean isAvailable = inventoryClient.checkStock(cart.getProductCode());
            if (!isAvailable) {
                LOGGER.error("some item not in stock");
                return false; // If any item is out of stock, return false
            }
        return true; // All items are in stock
    }

    // Fallback method in case inventory service fails
    public boolean checkStockFallback(Cart cart, Throwable ex) {
        LOGGER.error("Inventory service is unavailable, using fallback! " + ex.getMessage());
        return false; // Assume out of stock if inventory service is unavailable
    }




    public void removeCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cartRepository.delete(cart);
    }
    /*
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setTotalPrice(BigDecimal.ZERO);
            return cartRepository.save(newCart);
        });
    }

    public Cart addToCart(Long userId, String productCode, Integer quantity, BigDecimal price) {
        Cart cart = getCartByUserId(userId);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .productCode(productCode)
                .quantity(quantity)
                .price(price.multiply(BigDecimal.valueOf(quantity)))
                .build();

        cart.getItems().add(cartItem);
        cart.setTotalPrice(cart.getTotalPrice().add(cartItem.getPrice()));

        cartItemRepository.save(cartItem);
        return cartRepository.save(cart);
    }


    public void checkout(Long userId) {
        Cart cart = getCartByUserId(userId);
        System.out.println("Checkout completed for User ID: " + userId);
        cartRepository.delete(cart);
    }
     */
}
