package _CC3S2B.Spring.TawsBackend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestDTO {

    @NotBlank(message = "La ciudad de partida es obligatoria")
    private String departureCity;

    @NotBlank(message = "El destino es obligatorio")
    private String destination;

    @NotNull(message = "La cantidad de pasajeros es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 pasajero")
    private Integer passengerCount;

    @NotNull(message = "La fecha de partida es obligatoria")
    @Future(message = "La fecha de partida debe ser una fecha futura")
    private LocalDateTime departureDate;

    @NotNull(message = "La fecha de retorno es obligatoria")
    @Future(message = "La fecha de retorno debe ser una fecha futura")
    private LocalDateTime returnDate;
}