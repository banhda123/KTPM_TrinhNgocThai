package com.ecommerce.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    private Long orderId;
    private String carrierName;
    private String shippingAddress;
    private String notes;
} 