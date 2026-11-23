package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;

@Builder
public record PaymentStatusResponse(
        String status,
        String receiptType,
        String documentNumber,
        Long transactionId
) {
    public PaymentStatusResponse(String status) {
        this(status, null, null, null);
    }
}
