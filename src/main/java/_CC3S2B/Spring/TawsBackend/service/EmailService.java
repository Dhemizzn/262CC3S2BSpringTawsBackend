package _CC3S2B.Spring.TawsBackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    private final RestClient restClient;

    public EmailService() {
        this.restClient = RestClient.builder().baseUrl("https://api.brevo.com/v3").build();
    }

    public void sendWelcomeEmail(String recipient, String firstName, String temporaryPassword) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "TAWS", "email", senderEmail),
                "to", List.of(Map.of("email", recipient)),
                "subject", "¡Bienvenido a TAWS!",
                "textContent", """
                ¡Hola, %s!

                Te damos la bienvenida al sistema TAWS.
                Tu cuenta ha sido creada correctamente.

                Tus credenciales de acceso son:

                Contraseña temporal: %s

                Por seguridad, te recomendamos cambiar tu contraseña después de iniciar sesión.

                ¡Gracias por formar parte de TAWS!

                Equipo TAWS.
                """.formatted(firstName, recipient, temporaryPassword)
        );

        restClient.post()
                .uri("/smtp/email")
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}