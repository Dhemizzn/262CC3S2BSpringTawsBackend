package _CC3S2B.Spring.TawsBackend.dto;

public record CheckoutRequest(
        String quotationId,
        String receiptType,
        String dni,
        String ruc,
        String businessName,
        String fiscalAddress
) {
    public CheckoutRequest(String quotationId) {
        this(quotationId, null, null, null, null, null);
    }
}
