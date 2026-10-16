package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
@Builder
public record AirplaneView(
        Integer id,
        String airline,
        BigDecimal roundTripPricePerPerson,
        LocalTime outboundTime,
        LocalTime returnTime
) {}