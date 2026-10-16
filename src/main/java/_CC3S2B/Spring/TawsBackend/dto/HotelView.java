package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record HotelView(
        Integer id,
        String name,
        BigDecimal pricePerNight,
        int rooms,
        int nights
) {}