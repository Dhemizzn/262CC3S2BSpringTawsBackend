package _CC3S2B.Spring.TawsBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPackageResponse {
    // Campos originales de Quotation
    private Integer numId;
    private String quotationId;
    private String status;
    private Integer peopleQuantity;
    private String departureRegion;
    private String destinationRegion;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalTime departureTime;
    private LocalTime returnTime;
    private BigDecimal total;
    private Integer appUserId;
    private String itinerary;
    private String mpOrderId;
    private String mpCheckoutUrl;
    private String mpIdempotencyKey;

    // Campos del comprobante emitido
    private Long transactionId;
    private String receiptType;
    private String documentNumber;
    private LocalDate transactionDate;
    private String ruc;
    private String businessName;
    private String fiscalAddress;
}
