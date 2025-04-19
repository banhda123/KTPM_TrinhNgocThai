package com.ecommerce.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/products")
    public Mono<ResponseEntity<Map<String, String>>> productServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Product Service is currently unavailable. Please try again later.");
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/payments")
    public Mono<ResponseEntity<Map<String, String>>> paymentServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Payment Service is currently unavailable. Please try again later.");
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/inventory")
    public Mono<ResponseEntity<Map<String, String>>> inventoryServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Inventory Service is currently unavailable. Please try again later.");
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/shipping")
    public Mono<ResponseEntity<Map<String, String>>> shippingServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Shipping Service is currently unavailable. Please try again later.");
        return Mono.just(ResponseEntity.ok(response));
    }
} 