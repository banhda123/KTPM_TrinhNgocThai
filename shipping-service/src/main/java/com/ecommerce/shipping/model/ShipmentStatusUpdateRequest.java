package com.ecommerce.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdateRequest {
    private ShipmentStatus status;
    private String notes;
} 