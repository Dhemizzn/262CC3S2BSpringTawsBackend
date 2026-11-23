package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record AdminPackageDTO(
        String quotationId,
        String status,
        Integer peopleQuantity,
        String departureRegion,
        String destinationRegion,
        LocalDate departureDate,
        LocalDate returnDate,
        BigDecimal total,
        Integer userId,
        String clientName,
        String clientDni,
        String clientEmail,
        String mpOrderId,
        String itinerary
) {}
