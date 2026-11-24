package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.CheckoutRequest;
import _CC3S2B.Spring.TawsBackend.dto.CheckoutResponse;
import _CC3S2B.Spring.TawsBackend.dto.PaymentStatusResponse;
import _CC3S2B.Spring.TawsBackend.model.*;
import _CC3S2B.Spring.TawsBackend.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;
    private final RucRepository rucRepository;
    private final DniRepository dniRepository;
    private final TransactionRepository transactionRepository;
    private final ReceiptRepository receiptRepository;
    private final InvoiceRepository invoiceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mercadopago.access-token}")
    private String accessToken;
    private final RestClient restClient = RestClient.create("https://api.mercadopago.com");

    @Transactional
    public CheckoutResponse createCheckout(CheckoutRequest checkoutRequest, Integer userId) {
        String quotationId = validateQuotationId(checkoutRequest);
        Quotation quotation = findQuotation(quotationId, userId);

        if (!"pending".equals(quotation.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La cotización no está pendiente"
            );
        }

        saveBillingInfoIfPresent(quotation, checkoutRequest);

        if (quotation.getMpOrderId() != null &&
                quotation.getMpCheckoutUrl() != null) {
            return CheckoutResponse.builder()
                    .orderId(quotation.getMpOrderId())
                    .checkoutUrl(quotation.getMpCheckoutUrl())
                    .build();
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

        return CheckoutResponse.builder()
                .orderId(orderId)
                .checkoutUrl(checkoutUrl)
                .build();
    }

    public CheckoutResponse createCheckout(String quotationId, Integer userId) {
        return createCheckout(new CheckoutRequest(quotationId), userId);
    }

    @Transactional
    public PaymentStatusResponse verifyPayment(CheckoutRequest checkoutRequest, Integer userId) {
        String quotationId = validateQuotationId(checkoutRequest);
        Quotation quotation = findQuotation(quotationId, userId);

        saveBillingInfoIfPresent(quotation, checkoutRequest);

        if ("paid".equals(quotation.getStatus())) {
            return buildPaidResponse(quotation);
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

            processReceiptGeneration(quotation);

            return buildPaidResponse(quotation);
        }

        return PaymentStatusResponse.builder()
                .status(quotation.getStatus())
                .build();
    }

    public PaymentStatusResponse verifyPayment(String quotationId, Integer userId) {
        return verifyPayment(new CheckoutRequest(quotationId), userId);
    }

    private void processReceiptGeneration(Quotation quotation) {
        Transaction transaction = transactionRepository.findByQuotationId(quotation.getQuotationId())
                .orElseGet(() -> {
                    Transaction newTx = Transaction.builder()
                            .date(LocalDate.now())
                            .amount(quotation.getTotal())
                            .quotationId(quotation.getQuotationId())
                            .packageId(quotation.getQuotationId().length() <= 9 ? quotation.getQuotationId() : quotation.getQuotationId().substring(0, 9))
                            .build();
                    return transactionRepository.saveAndFlush(newTx);
                });

        Map<String, Object> billingInfo = extractBillingInfo(quotation);
        String receiptType = (String) billingInfo.getOrDefault("receiptType", "boleta");

        if ("factura".equalsIgnoreCase(receiptType)) {
            Optional<Invoice> existingInvoice = invoiceRepository.findByTransaction_TransactionId(transaction.getTransactionId());
            if (existingInvoice.isEmpty()) {
                String ruc = (String) billingInfo.get("ruc");
                String businessName = (String) billingInfo.get("businessName");
                String fiscalAddress = (String) billingInfo.get("fiscalAddress");

                if (ruc != null && !ruc.isBlank()) {
                    String cleanRuc = ruc.trim();
                    Integer userId = quotation.getAppUserId();
                    if (userId != null) {
                        Optional<Ruc> existingRucOpt = rucRepository.findByUser_UserIdAndRucNumber(userId, cleanRuc);
                        if (existingRucOpt.isPresent()) {
                            Ruc existingRuc = existingRucOpt.get();
                            existingRuc.setBusinessName(businessName != null ? businessName.trim() : "");
                            existingRuc.setFiscalAddress(fiscalAddress != null ? fiscalAddress.trim() : "");
                            existingRuc.setUpdatedAt(LocalDateTime.now());
                            rucRepository.save(existingRuc);
                        } else {
                            User user = userRepository.findById(userId).orElse(null);
                            if (user != null) {
                                Ruc newRuc = Ruc.builder()
                                        .user(user)
                                        .rucNumber(cleanRuc)
                                        .businessName(businessName != null ? businessName.trim() : "")
                                        .fiscalAddress(fiscalAddress != null ? fiscalAddress.trim() : "")
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                                rucRepository.save(newRuc);
                            }
                        }
                    }
                }

                String nextInvoiceNumber = generateNextInvoiceNumber();
                Invoice invoice = Invoice.builder()
                        .transactionId(transaction.getTransactionId())
                        .transaction(transaction)
                        .invoiceNumber(nextInvoiceNumber)
                        .ruc(ruc != null ? ruc.trim() : "")
                        .businessName(businessName != null ? businessName.trim() : "")
                        .fiscalAddress(fiscalAddress != null ? fiscalAddress.trim() : "")
                        .isNew(true)
                        .build();
                invoiceRepository.save(invoice);
            }
        } else {
            Optional<Receipt> existingReceipt = receiptRepository.findByTransaction_TransactionId(transaction.getTransactionId());
            if (existingReceipt.isEmpty()) {
                String dni = (String) billingInfo.get("dni");
                if (dni != null && !dni.isBlank()) {
                    String normDni = dni.replaceAll("\\s+", "");
                    if (normDni.length() > 8) normDni = normDni.substring(0, 8);
                    if (!dniRepository.existsById(normDni)) {
                        dniRepository.save(Dni.builder().dniNumber(normDni).build());
                    }
                }

                String nextReceiptNumber = generateNextReceiptNumber();
                Receipt receipt = Receipt.builder()
                        .transactionId(transaction.getTransactionId())
                        .transaction(transaction)
                        .receiptNumber(nextReceiptNumber)
                        .isNew(true)
                        .build();
                receiptRepository.save(receipt);
            }
        }
    }

    private PaymentStatusResponse buildPaidResponse(Quotation quotation) {
        Optional<Transaction> txOpt = transactionRepository.findByQuotationId(quotation.getQuotationId());
        if (txOpt.isEmpty()) {
            return PaymentStatusResponse.builder()
                    .status("paid")
                    .build();
        }

        Transaction tx = txOpt.get();
        Optional<Invoice> invoiceOpt = invoiceRepository.findByTransaction_TransactionId(tx.getTransactionId());
        if (invoiceOpt.isPresent()) {
            return PaymentStatusResponse.builder()
                    .status("paid")
                    .receiptType("factura")
                    .documentNumber(invoiceOpt.get().getInvoiceNumber())
                    .transactionId(tx.getTransactionId())
                    .build();
        }

        Optional<Receipt> receiptOpt = receiptRepository.findByTransaction_TransactionId(tx.getTransactionId());
        if (receiptOpt.isPresent()) {
            return PaymentStatusResponse.builder()
                    .status("paid")
                    .receiptType("boleta")
                    .documentNumber(receiptOpt.get().getReceiptNumber())
                    .transactionId(tx.getTransactionId())
                    .build();
        }

        return PaymentStatusResponse.builder()
                .status("paid")
                .transactionId(tx.getTransactionId())
                .build();
    }

    private synchronized String generateNextReceiptNumber() {
        String last = receiptRepository.findLastReceiptNumber();
        if (last == null || !last.matches("B\\d{3}-\\d{8}")) {
            return "B001-00000001";
        }
        try {
            String[] parts = last.split("-");
            long nextSeq = Long.parseLong(parts[1]) + 1;
            return String.format("%s-%08d", parts[0], nextSeq);
        } catch (Exception e) {
            return "B001-00000001";
        }
    }

    private synchronized String generateNextInvoiceNumber() {
        String last = invoiceRepository.findLastInvoiceNumber();
        if (last == null || !last.matches("F\\d{3}-\\d{8}")) {
            return "F001-00000001";
        }
        try {
            String[] parts = last.split("-");
            long nextSeq = Long.parseLong(parts[1]) + 1;
            return String.format("%s-%08d", parts[0], nextSeq);
        } catch (Exception e) {
            return "F001-00000001";
        }
    }

    private void saveBillingInfoIfPresent(Quotation quotation, CheckoutRequest req) {
        if (req.receiptType() == null || req.receiptType().isBlank()) {
            return;
        }

        validateBillingData(req);

        try {
            Map<String, Object> map = new HashMap<>();
            if (quotation.getItinerary() != null && !quotation.getItinerary().isBlank()) {
                try {
                    map = objectMapper.readValue(quotation.getItinerary(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception ignored) {
                    map = new HashMap<>();
                }
            }

            Map<String, Object> billingInfo = new HashMap<>();
            billingInfo.put("receiptType", req.receiptType().trim().toLowerCase());
            if (req.dni() != null) billingInfo.put("dni", req.dni().trim());
            if (req.ruc() != null) billingInfo.put("ruc", req.ruc().trim());
            if (req.businessName() != null) billingInfo.put("businessName", req.businessName().trim());
            if (req.fiscalAddress() != null) billingInfo.put("fiscalAddress", req.fiscalAddress().trim());

            map.put("billingInfo", billingInfo);
            quotation.setItinerary(objectMapper.writeValueAsString(map));
            quotationRepository.save(quotation);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar datos de facturacion", e);
        }
    }

    private void validateBillingData(CheckoutRequest req) {
        String type = req.receiptType().trim().toLowerCase();
        if ("boleta".equals(type)) {
            if (req.dni() == null || !req.dni().trim().matches("^\\d{8}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para boleta se requiere un DNI valido de 8 digitos");
            }
        } else if ("factura".equals(type)) {
            if (req.ruc() == null || !req.ruc().trim().matches("^\\d{11}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para factura se requiere un RUC valido de 11 digitos");
            }
            if (req.businessName() == null || req.businessName().trim().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para factura se requiere la razon social");
            }
            if (req.fiscalAddress() == null || req.fiscalAddress().trim().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para factura se requiere la direccion fiscal");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de comprobante invalido. Use 'boleta' o 'factura'");
        }
    }

    private Map<String, Object> extractBillingInfo(Quotation quotation) {
        if (quotation.getItinerary() != null && !quotation.getItinerary().isBlank()) {
            try {
                Map<String, Object> map = objectMapper.readValue(quotation.getItinerary(), new TypeReference<Map<String, Object>>() {});
                Object billingObj = map.get("billingInfo");
                if (billingObj instanceof Map<?, ?> bMap) {
                    Map<String, Object> result = new HashMap<>();
                    bMap.forEach((k, v) -> result.put(String.valueOf(k), v));
                    return result;
                }
            } catch (Exception ignored) {
            }
        }
        return Map.of("receiptType", "boleta");
    }

    public Ruc findRuc(String rucNumber) {
        if (rucNumber == null || rucNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC es obligatorio");
        }
        return rucRepository.findFirstByRucNumberOrderByUpdatedAtDesc(rucNumber.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RUC no encontrado"));
    }

    public Ruc getLastRuc(Integer userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        return rucRepository.findFirstByUser_UserIdOrderByUpdatedAtDesc(userId)
                .orElse(null);
    }

    private String validateQuotationId(CheckoutRequest request) {
        if (request == null ||
                request.quotationId() == null ||
                request.quotationId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cotización es obligatoria"
            );
        }
        return request.quotationId().trim();
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
}
