package _CC3S2B.Spring.TawsBackend.security;


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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/quotes/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception  //Permite configurar cómo Spring Security responde ante determinados errores de seguridad.
                        //define cómo responder cuando se necesita autenticación y el usuario no está autenticado correctamente.
                        //Authexception,la excepción relacionada con la necesidad de autenticación.
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED); //Aquí estableces el estado HTTP de la respuesta con error 404
                            response.setContentType("application/json;charset=UTF-8");//Le indica al cliente que el cuerpo de la respuesta será JSON y que se utilizará la codificación UTF-8.
                            java.util.Map<String, Object> body = new java.util.HashMap<>(); //Aquí creas un mapa Java para almacenar los datos que quieres devolver al cliente.
                            body.put("timestamp", java.time.LocalDateTime.now().toString());//LocalDateTime.now() obtiene la fecha y hora local del servidor, y toString() la convierte en texto.
                            body.put("status", 401);//Añade el código HTTP al JSON para que el frontend pueda identificar el tipo de error.
                            body.put("error", "Unauthorized");//Añade una descripción breve del error.
                            body.put("message", "Acceso no autorizado. Debe proporcionar un token válido.");//Añade un mensaje que explica al cliente por qué no puede acceder.
                            new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getOutputStream(), body);
                            //crea un objeto ObjectMapper, una clase de Jackson que permite convertir objetos Java a JSON y viceversa.
                            //writeValue(), que serializa el mapa body a JSON y escribe el resultado directamente en el flujo de salida de la respuesta HTTP.
                            //permite escribir los bytes del cuerpo de la respuesta que recibirá el cliente.
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
