package com.ecommerce.shipping.controller;

import com.ecommerce.shipping.model.ShipmentRequest;
import com.ecommerce.shipping.model.ShipmentResponse;
import com.ecommerce.shipping.model.ShipmentStatus;
import com.ecommerce.shipping.model.ShipmentStatusUpdateRequest;
import com.ecommerce.shipping.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    @Autowired
    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@RequestBody ShipmentRequest shipmentRequest) {
        ShipmentResponse response = shippingService.createShipment(shipmentRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        ShipmentResponse response = shippingService.getShipmentById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByOrderId(@PathVariable Long orderId) {
        List<ShipmentResponse> responses = shippingService.getShipmentsByOrderId(orderId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByStatus(@PathVariable ShipmentStatus status) {
        List<ShipmentResponse> responses = shippingService.getShipmentsByStatus(status);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @PathVariable Long id,
            @RequestBody ShipmentStatusUpdateRequest updateRequest) {
        ShipmentResponse response = shippingService.updateShipmentStatus(id, updateRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ShipmentResponse> cancelShipment(@PathVariable Long id) {
        ShipmentResponse response = shippingService.cancelShipment(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
} 