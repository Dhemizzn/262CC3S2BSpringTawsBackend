package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Builder
public record QuoteResponse(
        UUID proposalId,
        String departureCity,
        String destination,
        int passengerCount,
        LocalDate departureDate,
        LocalDate returnDate,
        int days,
        HotelView hotel,
        AirplaneView airplane,
        List<ActivityView> activities,
        BigDecimal totalPrice
) {}