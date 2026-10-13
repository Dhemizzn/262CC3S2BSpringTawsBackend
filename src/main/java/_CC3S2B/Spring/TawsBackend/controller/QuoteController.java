package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.dto.QuoteRequestDTO;
import _CC3S2B.Spring.TawsBackend.dto.QuoteResponseDTO;
import _CC3S2B.Spring.TawsBackend.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponseDTO> createQuote(@Valid @RequestBody QuoteRequestDTO quoteRequestDTO) {
        QuoteResponseDTO response = quoteService.createQuoteRequest(quoteRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}