package _CC3S2B.Spring.TawsBackend.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tawsApi() {

        String jwt = "jwtAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("TAWS API")
                        .version("1.0")
                        .description("Documentación de los servicios REST del proyecto TAWS"))
                .components(new Components()
                        .addSecuritySchemes(jwt, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(jwt));
    }
}