package _CC3S2B.Spring.TawsBackend.dto;

import java.time.*;
import java.math.BigDecimal;
public record ActivityView(Integer id, String name, LocalDate date, LocalTime startTime, LocalTime endTime, int durationMinutes, BigDecimal pricePerPerson) {}
