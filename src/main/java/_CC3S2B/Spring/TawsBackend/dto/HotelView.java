package _CC3S2B.Spring.TawsBackend.dto;

import java.math.BigDecimal;

public record HotelView(
        Integer id,
        String name,
        BigDecimal pricePerNight,
        int rooms,
        int nights
) {}