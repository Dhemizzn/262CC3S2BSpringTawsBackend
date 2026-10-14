package _CC3S2B.Spring.TawsBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MyPackageView(
        String packageId,
        String quotationId,
        String status,
        BigDecimal totalPrice,
        LocalDate departureDate,
        LocalDate returnDate,
        String destination
) {}