package _CC3S2B.Spring.TawsBackend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record QuoteRequest(
        @NotBlank
        String departureCity,
        @NotBlank
        String destination,
        @NotNull
        @Min(1)
        Integer passengerCount,
        @NotNull
        LocalDate departureDate,
        @NotNull
        LocalDate returnDate
) {}