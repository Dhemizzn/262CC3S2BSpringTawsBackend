package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;

@Builder
public record CheckoutResponse(
        String orderId,
        String checkoutUrl
) {}
