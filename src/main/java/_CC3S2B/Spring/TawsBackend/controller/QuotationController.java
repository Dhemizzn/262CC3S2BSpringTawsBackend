package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.dto.QuoteRequest;
import _CC3S2B.Spring.TawsBackend.dto.QuoteResponse;
import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping("/destinations")
    public List<DestinationOption> destinations() {
        return quotationService.destinations().stream()
                .map(d -> new DestinationOption(d.getDestinationId(), d.getName()))
                .toList();
    }

    @PostMapping("/generate")
    public List<QuoteResponse> generate(@Valid @RequestBody QuoteRequest request) {
        return quotationService.generate(request);
    }

    @PostMapping("/confirm")
    public Map<String, String> confirm(@RequestBody QuoteResponse selected,
                                       Authentication authentication) {
        Integer userId = authenticated(authentication);
        String quotationId = quotationService.confirm(selected, userId);
        return Map.of(
                "quotationId", quotationId,
                "status", "pending",
                "message", "Cotizacion guardada correctamente"
        );
    }

    private Integer authenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesion");
        }
        return user.getUserId();
    }

    public record DestinationOption(Integer id, String name) {}
}
