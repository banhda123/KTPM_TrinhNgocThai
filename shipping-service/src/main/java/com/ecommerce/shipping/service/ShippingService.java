package com.ecommerce.shipping.service;

import com.ecommerce.shipping.model.ShipmentRequest;
import com.ecommerce.shipping.model.ShipmentResponse;
import com.ecommerce.shipping.model.ShipmentStatus;
import com.ecommerce.shipping.model.ShipmentStatusUpdateRequest;

import java.util.List;

public interface ShippingService {
    ShipmentResponse createShipment(ShipmentRequest shipmentRequest);
    ShipmentResponse getShipmentById(Long shipmentId);
    List<ShipmentResponse> getShipmentsByOrderId(Long orderId);
    List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status);
    ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatusUpdateRequest updateRequest);
    ShipmentResponse cancelShipment(Long shipmentId);
} 