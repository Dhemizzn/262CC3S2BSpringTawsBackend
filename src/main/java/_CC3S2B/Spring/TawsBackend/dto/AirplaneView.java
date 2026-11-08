package _CC3S2B.Spring.TawsBackend.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record AirplaneView(
        Integer id,
        String airline,
        BigDecimal roundTripPricePerPerson,
        LocalTime outboundTime,
        LocalTime returnTime
) {}