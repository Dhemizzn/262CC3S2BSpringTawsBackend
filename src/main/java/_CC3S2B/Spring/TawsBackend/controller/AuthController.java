
package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.dto.AuthResponseDTO;
import _CC3S2B.Spring.TawsBackend.dto.LoginRequestDTO;
import _CC3S2B.Spring.TawsBackend.dto.RegisterRequestDTO;
import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDTO login(
            @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponseDTO register(
            @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        User user = (User) auth.getPrincipal();

        return Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "authenticated", auth.isAuthenticated()
        );
    }
}
