package _CC3S2B.Spring.TawsBackend.security;


import _CC3S2B.Spring.TawsBackend.util.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");

                            ErrorResponse error = ErrorResponse.builder()
                                    .status(401)
                                    .error("Unauthorized")
                                    .message("Acceso no autorizado. Debe proporcionar un token válido.")
                                    .build();
                            objectMapper.writeValue(response.getOutputStream(), error);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);//Esta instrucción construye y devuelve el objeto SecurityFilterChain con las configuraciones que definiste anteriormente.
        return http.build();
    }
    @Bean //un objeto que Spring administra y que puede inyectar en otras clases
    public PasswordEncoder passwordEncoder()  { //es una interfaz de Spring Security que define operaciones para codificar contraseñas y comprobar si una contraseña coincide con su versión codificada
        return new BCryptPasswordEncoder(); //BCrypt permite almacenar las contraseñas mediante hashes, evitando guardar las contraseñas originales en texto plano.
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {  //controlar qué solicitudes realizadas desde otros orígenes pueden acceder a tus endpoints a través de un navegador
        return new WebMvcConfigurer() { //devuelve un objeto que implementa WebMvcConfigurer, una interfaz que permite personalizar la configuración de Spring MVC.
            @Override
            public void addCorsMappings(CorsRegistry registry) { //permite registrar las reglas CORS que Spring MVC utilizará para las rutas de la aplicación
                registry.addMapping("/**") //Indica que esta configuración CORS se aplica a todas las rutas que coincidan con el patrón /**.
                        .allowedOrigins("*")//Permite solicitudes CORS desde cualquier origen.
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") //Define los métodos HTTP que se permiten mediante CORS.
                        .allowedHeaders("*"); //Permite cualquier encabezado HTTP en las solicitudes CORS.
            }
        };
    }


}
