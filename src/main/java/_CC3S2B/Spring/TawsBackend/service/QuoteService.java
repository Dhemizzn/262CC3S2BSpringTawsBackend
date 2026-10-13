package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.QuoteRequestDTO;
import _CC3S2B.Spring.TawsBackend.dto.QuoteResponseDTO;
import _CC3S2B.Spring.TawsBackend.model.QuoteRequest;
import _CC3S2B.Spring.TawsBackend.repository.QuoteRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRequestRepository quoteRequestRepository;

    @Transactional
    public QuoteResponseDTO createQuoteRequest(QuoteRequestDTO dto) {
        if (dto.getReturnDate().isBefore(dto.getDepartureDate())) {
            throw new IllegalArgumentException("La fecha de retorno debe ser posterior a la fecha de partida");
        }

        QuoteRequest quoteRequest = QuoteRequest.builder()
                .departureCity(dto.getDepartureCity())
                .destination(dto.getDestination())
                .passengerCount(dto.getPassengerCount())
                .departureDate(dto.getDepartureDate())
                .returnDate(dto.getReturnDate())
                .status("PENDING")
                .build();

        QuoteRequest savedQuote = quoteRequestRepository.save(quoteRequest);

        return QuoteResponseDTO.builder()
                .id(savedQuote.getId())
                .departureCity(savedQuote.getDepartureCity())
                .destination(savedQuote.getDestination())
                .passengerCount(savedQuote.getPassengerCount())
                .departureDate(savedQuote.getDepartureDate())
                .returnDate(savedQuote.getReturnDate())
                .status(savedQuote.getStatus())
                .createdAt(savedQuote.getCreatedAt())
                .message("Solicitud de cotización registrada con éxito")
                .build();
    }
}