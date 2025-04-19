package com.ecommerce.shipping.service;

import com.ecommerce.shipping.model.Shipment;
import com.ecommerce.shipping.model.ShipmentRequest;
import com.ecommerce.shipping.model.ShipmentResponse;
import com.ecommerce.shipping.model.ShipmentStatus;
import com.ecommerce.shipping.model.ShipmentStatusUpdateRequest;
import com.ecommerce.shipping.repository.ShipmentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ShippingServiceImpl(ShipmentRepository shipmentRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.shipmentRepository = shipmentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @CircuitBreaker(name = "shippingService", fallbackMethod = "createShipmentFallback")
    @RateLimiter(name = "shippingService")
    @Retry(name = "shippingService")
    public ShipmentResponse createShipment(ShipmentRequest shipmentRequest) {
        try {
            Shipment shipment = new Shipment();
            shipment.setOrderId(shipmentRequest.getOrderId());
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment.setCarrierName(shipmentRequest.getCarrierName());
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setShippingAddress(shipmentRequest.getShippingAddress());
            shipment.setCreatedAt(LocalDateTime.now());
            shipment.setUpdatedAt(LocalDateTime.now());
            shipment.setNotes(shipmentRequest.getNotes());
            
            shipment = shipmentRepository.save(shipment);
            
            // Notify other services about the new shipment
            kafkaTemplate.send("shipping-events", "Shipment created: " + shipment.getId());
            
            return mapToShipmentResponse(shipment, "Shipment created successfully");
        } catch (Exception e) {
            return new ShipmentResponse(null, shipmentRequest.getOrderId(), null, 
                    shipmentRequest.getCarrierName(), ShipmentStatus.PENDING, 
                    shipmentRequest.getShippingAddress(), null, null, null, null, 
                    shipmentRequest.getNotes(), "Error creating shipment: " + e.getMessage());
        }
    }

    public ShipmentResponse createShipmentFallback(ShipmentRequest shipmentRequest, Exception e) {
        return new ShipmentResponse(null, shipmentRequest.getOrderId(), null, 
                shipmentRequest.getCarrierName(), ShipmentStatus.PENDING, 
                shipmentRequest.getShippingAddress(), null, null, null, null, 
                shipmentRequest.getNotes(), "Shipping service is currently unavailable. Please try again later.");
    }

    @Override
    @CircuitBreaker(name = "shippingService", fallbackMethod = "getShipmentByIdFallback")
    @RateLimiter(name = "shippingService")
    public ShipmentResponse getShipmentById(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));
        return mapToShipmentResponse(shipment, "Shipment retrieved successfully");
    }

    public ShipmentResponse getShipmentByIdFallback(Long shipmentId, Exception e) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipmentId);
        response.setMessage("Shipping service is currently unavailable. Please try again later.");
        return response;
    }

    @Override
    @CircuitBreaker(name = "shippingService", fallbackMethod = "getShipmentsByOrderIdFallback")
    @RateLimiter(name = "shippingService")
    public List<ShipmentResponse> getShipmentsByOrderId(Long orderId) {
        List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
        return shipments.stream()
                .map(shipment -> mapToShipmentResponse(shipment, "Shipment retrieved successfully"))
                .collect(Collectors.toList());
    }

    public List<ShipmentResponse> getShipmentsByOrderIdFallback(Long orderId, Exception e) {
        ShipmentResponse response = new ShipmentResponse();
        response.setOrderId(orderId);
        response.setMessage("Shipping service is currently unavailable. Please try again later.");
        return List.of(response);
    }

    @Override
    @CircuitBreaker(name = "shippingService", fallbackMethod = "getShipmentsByStatusFallback")
    @RateLimiter(name = "shippingService")
    public List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status) {
        List<Shipment> shipments = shipmentRepository.findByStatus(status);
        return shipments.stream()
                .map(shipment -> mapToShipmentResponse(shipment, "Shipments retrieved successfully"))
                .collect(Collectors.toList());
    }

    public List<ShipmentResponse> getShipmentsByStatusFallback(ShipmentStatus status, Exception e) {
        ShipmentResponse response = new ShipmentResponse();
        response.setStatus(status);
        response.setMessage("Shipping service is currently unavailable. Please try again later.");
        return List.of(response);
    }

    @Override
    @CircuitBreaker(name = "shippingService", fallbackMethod = "updateShipmentStatusFallback")
    @RateLimiter(name = "shippingService")
    @Retry(name = "shippingService")
    public ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatusUpdateRequest updateRequest) {
        try {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));
            
            shipment.setStatus(updateRequest.getStatus());
            shipment.setUpdatedAt(LocalDateTime.now());
            
            if (updateRequest.getNotes() != null) {
                shipment.setNotes(updateRequest.getNotes());
            }
            
            // Update specific timestamps based on the status
            if (updateRequest.getStatus() == ShipmentStatus.SHIPPED) {
                shipment.setShippedAt(LocalDateTime.now());
            } else if (updateRequest.getStatus() == ShipmentStatus.DELIVERED) {
                shipment.setDeliveredAt(LocalDateTime.now());
            }
            
            shipment = shipmentRepository.save(shipment);
            
            // Notify other services about the status update
            kafkaTemplate.send("shipping-events", "Shipment status updated: " + shipment.getId() + ", Status: " + shipment.getStatus());
            
            return mapToShipmentResponse(shipment, "Shipment status updated successfully");
        } catch (Exception e) {
            return new ShipmentResponse(shipmentId, null, null, null, updateRequest.getStatus(), 
                    null, null, null, null, null, updateRequest.getNotes(), 
                    "Error updating shipment status: " + e.getMessage());
        }
    }

    public ShipmentResponse updateShipmentStatusFallback(Long shipmentId, ShipmentStatusUpdateRequest updateRequest, Exception e) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipmentId);
        response.setStatus(updateRequest.getStatus());
        response.setMessage("Shipping service is currently unavailable. Please try again later.");
        return response;
    }

    @Override
    @CircuitBreaker(name = "shippingService", fallbackMethod = "cancelShipmentFallback")
    @RateLimiter(name = "shippingService")
    @Retry(name = "shippingService")
    public ShipmentResponse cancelShipment(Long shipmentId) {
        try {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));
            
            // Only allow cancellation if not already shipped or delivered
            if (shipment.getStatus() == ShipmentStatus.SHIPPED ||
                shipment.getStatus() == ShipmentStatus.IN_TRANSIT ||
                shipment.getStatus() == ShipmentStatus.DELIVERED) {
                return mapToShipmentResponse(shipment, "Cannot cancel shipment that has already been " + shipment.getStatus());
            }
            
            shipment.setStatus(ShipmentStatus.CANCELLED);
            shipment.setUpdatedAt(LocalDateTime.now());
            
            shipment = shipmentRepository.save(shipment);
            
            // Notify other services about the cancellation
            kafkaTemplate.send("shipping-events", "Shipment cancelled: " + shipment.getId());
            
            return mapToShipmentResponse(shipment, "Shipment cancelled successfully");
        } catch (Exception e) {
            return new ShipmentResponse(shipmentId, null, null, null, ShipmentStatus.CANCELLED, 
                    null, null, null, null, null, null, 
                    "Error cancelling shipment: " + e.getMessage());
        }
    }

    public ShipmentResponse cancelShipmentFallback(Long shipmentId, Exception e) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipmentId);
        response.setStatus(ShipmentStatus.CANCELLED);
        response.setMessage("Shipping service is currently unavailable. Please try again later.");
        return response;
    }
    
    private String generateTrackingNumber() {
        // Simple tracking number generator - in a real system this would be more sophisticated
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private ShipmentResponse mapToShipmentResponse(Shipment shipment, String message) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipment.getId());
        response.setOrderId(shipment.getOrderId());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setCarrierName(shipment.getCarrierName());
        response.setStatus(shipment.getStatus());
        response.setShippingAddress(shipment.getShippingAddress());
        response.setCreatedAt(shipment.getCreatedAt());
        response.setUpdatedAt(shipment.getUpdatedAt());
        response.setShippedAt(shipment.getShippedAt());
        response.setDeliveredAt(shipment.getDeliveredAt());
        response.setNotes(shipment.getNotes());
        response.setMessage(message);
        return response;
    }
} 