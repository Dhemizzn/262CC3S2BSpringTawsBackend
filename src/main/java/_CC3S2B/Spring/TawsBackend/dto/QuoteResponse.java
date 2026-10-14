package _CC3S2B.Spring.TawsBackend.dto;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public record QuoteResponse(UUID proposalId, String departureCity, String destination, int passengerCount, LocalDate departureDate, LocalDate returnDate, int days, HotelView hotel, AirplaneView airplane, List<ActivityView> activities, BigDecimal totalPrice) {}
