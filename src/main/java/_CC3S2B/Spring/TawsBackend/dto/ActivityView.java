package _CC3S2B.Spring.TawsBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ActivityView(
        Integer id,
        String name,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int durationMinutes,
        BigDecimal pricePerPerson
) {}