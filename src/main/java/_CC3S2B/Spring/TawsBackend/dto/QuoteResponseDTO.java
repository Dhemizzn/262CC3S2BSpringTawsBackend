package _CC3S2B.Spring.TawsBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDTO {

    private String id;
    private String departureCity;
    private String destination;
    private Integer passengerCount;
    private LocalDateTime departureDate;
    private LocalDateTime returnDate;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}