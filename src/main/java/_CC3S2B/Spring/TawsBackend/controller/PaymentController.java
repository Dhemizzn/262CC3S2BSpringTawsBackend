
package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public PaymentService.CheckoutResponse createCheckout(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.createCheckout(
                quotationId(request),
                userId(user)
        );
    }

    @PostMapping("/verify")
    public PaymentService.PaymentStatusResponse verifyPayment(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.verifyPayment(
                quotationId(request),
                userId(user)
        );
    }

    private Integer userId(User user) {
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Inicia sesión"
            );
        }

        return user.getUserId();
    }

    private String quotationId(CheckoutRequest request) {
        if (request == null ||
                request.quotationId() == null ||
                request.quotationId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cotización es obligatoria"
            );
        }

        return request.quotationId();
    }

    public record CheckoutRequest(String quotationId) {}
}
