
package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.model.Quotation;
import _CC3S2B.Spring.TawsBackend.repository.QuotationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final QuotationRepository quotationRepository;
    private final String accessToken;

    private final RestClient restClient =
            RestClient.create("https://api.mercadopago.com");

    public PaymentService(
            QuotationRepository quotationRepository,
            @Value("${mercadopago.access-token}") String accessToken
    ) {
        this.quotationRepository = quotationRepository;
        this.accessToken = accessToken;
    }

    public CheckoutResponse createCheckout(String quotationId, Integer userId) {

        Quotation quotation = findQuotation(quotationId, userId);

        if (!"pending".equals(quotation.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La cotización no está pendiente"
            );
        }

        if (quotation.getMpOrderId() != null &&
                quotation.getMpCheckoutUrl() != null) {
            return new CheckoutResponse(
                    quotation.getMpOrderId(),
                    quotation.getMpCheckoutUrl()
            );
        }

        String idempotencyKey = quotation.getMpIdempotencyKey();

        if (idempotencyKey == null) {
            idempotencyKey = UUID.randomUUID().toString();
            quotation.setMpIdempotencyKey(idempotencyKey);
            quotationRepository.saveAndFlush(quotation);
        }

        String amount = quotation.getTotal().toPlainString();

        Map<String, Object> request = Map.of(
                "type", "online",
                "processing_mode", "manual",
                "total_amount", amount,
                "external_reference", quotationId,
                "items", List.of(Map.of(
                        "title", "Paquete turístico " + quotationId,
                        "quantity", 1,
                        "unit_price", amount
                ))
        );

        Map<?, ?> response;

        try {
            response = restClient.post()
                    .uri("/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Idempotency-Key", idempotencyKey)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo crear la orden en Mercado Pago",
                    ex
            );
        }

        if (response == null ||
                !(response.get("id") instanceof String orderId) ||
                !(response.get("checkout_url") instanceof String checkoutUrl)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Mercado Pago no devolvió una orden válida"
            );
        }

        quotation.setMpOrderId(orderId);
        quotation.setMpCheckoutUrl(checkoutUrl);
        quotationRepository.save(quotation);

        return new CheckoutResponse(orderId, checkoutUrl);
    }

    public PaymentStatusResponse verifyPayment(String quotationId, Integer userId) {

        Quotation quotation = findQuotation(quotationId, userId);

        if ("paid".equals(quotation.getStatus())) {
            return new PaymentStatusResponse("paid");
        }

        if (!"pending".equals(quotation.getStatus()) ||
                quotation.getMpOrderId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cotización no tiene una orden pendiente"
            );
        }

        Map<?, ?> order;

        try {
            order = restClient.get()
                    .uri("/v1/orders/{id}", quotation.getMpOrderId())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo verificar la orden en Mercado Pago",
                    ex
            );
        }

        if (order == null ||
                !quotation.getMpOrderId().equals(order.get("id")) ||
                !quotationId.equals(order.get("external_reference")) ||
                !"PEN".equals(order.get("currency")) ||
                !sameAmount(order.get("total_amount"), quotation.getTotal())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Los datos de la orden no coinciden"
            );
        }

        if ("processed".equals(order.get("status")) &&
                "accredited".equals(order.get("status_detail"))) {

            if (!sameAmount(order.get("total_paid_amount"), quotation.getTotal())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "El importe pagado no coincide"
                );
            }

            quotation.setStatus("paid");
            quotationRepository.save(quotation);
        }

        return new PaymentStatusResponse(quotation.getStatus());
    }

    private Quotation findQuotation(String quotationId, Integer userId) {
        return quotationRepository
                .findByQuotationIdAndAppUserId(quotationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cotización no encontrada"
                ));
    }

    private boolean sameAmount(Object amount, BigDecimal expected) {
        if (amount == null) {
            return false;
        }

        try {
            return expected.compareTo(
                    new BigDecimal(amount.toString())
            ) == 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public record CheckoutResponse(
            String orderId,
            String checkoutUrl
    ) {}

    public record PaymentStatusResponse(String status) {}
}
