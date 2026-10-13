package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.QuoteRequestDTO;
import _CC3S2B.Spring.TawsBackend.dto.QuoteResponseDTO;
import _CC3S2B.Spring.TawsBackend.model.QuoteRequest;
import _CC3S2B.Spring.TawsBackend.repository.QuoteRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
                .departureRegion(dto.getDepartureCity())
                .destinationRegion(dto.getDestination())
                .peopleQuantity(dto.getPassengerCount())
                .departureDate(dto.getDepartureDate().toLocalDate())
                .departureTime(dto.getDepartureDate().toLocalTime())
                .returnDate(dto.getReturnDate().toLocalDate())
                .returnTime(dto.getReturnDate().toLocalTime())
                .status("pending")
                .total(BigDecimal.ZERO)
                .build();

        QuoteRequest savedQuote = quoteRequestRepository.saveAndFlush(quoteRequest);

        String responseId = savedQuote.getQuotationId() != null
                ? savedQuote.getQuotationId()
                : String.valueOf(savedQuote.getNumId());

        return QuoteResponseDTO.builder()
                .id(responseId)
                .departureCity(savedQuote.getDepartureRegion())
                .destination(savedQuote.getDestinationRegion())
                .passengerCount(savedQuote.getPeopleQuantity())
                .departureDate(LocalDateTime.of(savedQuote.getDepartureDate(), savedQuote.getDepartureTime()))
                .returnDate(LocalDateTime.of(savedQuote.getReturnDate(), savedQuote.getReturnTime()))
                .status(savedQuote.getStatus())
                .createdAt(LocalDateTime.now())
                .message("Solicitud de cotización registrada con éxito")
                .build();
    }
}