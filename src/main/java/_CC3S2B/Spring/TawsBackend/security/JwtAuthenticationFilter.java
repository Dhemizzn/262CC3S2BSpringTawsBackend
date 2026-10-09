package _CC3S2B.Spring.TawsBackend.security;


import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.repository.UserRepository;
import _CC3S2B.Spring.TawsBackend.service.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain; //Represetna la cadena de filtors por la que pasa una peticion HTTP.
import jakarta.servlet.ServletException; //Error relacionado con el procesamiento de una peticion mediante Servlet
import jakarta.servlet.http.HttpServletRequest; // Representa la peticion HTTP que llega al servidor
import jakarta.servlet.http.HttpServletResponse;// Representa la respuesta HTTP que el servidor enviara al cliente
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; //Base para crear un filtro HTTP que se ejecutauna vez por peticion

import java.io.IOException;
import java.util.Collections;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        final String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        try{
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);
            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                User user = userRepository.findByEmail(userEmail).orElse(null);

                if(user != null && jwtService.isTokenValid(jwt)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user,null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        }catch (JwtException|IllegalArgumentException io){
            SecurityContextHolder.clearContext();
        }
    }


}


