package org.acme.model;

public record OrderRiskRequest(
        String orderId,
        double amount,
        String customerId
) {
}
