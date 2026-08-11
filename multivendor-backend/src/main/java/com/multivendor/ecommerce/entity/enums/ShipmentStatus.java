package com.multivendor.ecommerce.entity.enums;

public enum ShipmentStatus {
    NOT_CREATED,      // no courier booking yet
    PICKUP_SCHEDULED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    RTO,               // return to origin (delivery failed / refused)
    CANCELLED,
    FAILED             // courier API call failed (see Shipment.lastError)
}
