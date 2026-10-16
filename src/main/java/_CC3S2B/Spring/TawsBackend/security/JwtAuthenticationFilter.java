package _CC3S2B.Spring.TawsBackend.security;

import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.model.Module;
import _CC3S2B.Spring.TawsBackend.repository.UserRepository;
import _CC3S2B.Spring.TawsBackend.repository.ModuleRepository;
import _CC3S2B.Spring.TawsBackend.service.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtService;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                final String jwt = authHeader.substring(7);
                final String userEmail = jwtService.extractUsername(jwt);


                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmail(userEmail).orElse(null);

                    if (user != null && jwtService.isTokenValid(jwt)) {
                        List<GrantedAuthority> authorities = new ArrayList<>();

                        if (user.getRol() != null) {
                            String rolName = user.getRol().getRolName().toUpperCase(Locale.ROOT);
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + rolName));

                            Integer rolId = user.getRol().getRolId();
                            List<Module> modules = moduleRepository.findByRoleId(rolId);

                            for (Module module : modules) {
                                String moduleName = module.getModuleName().toUpperCase(Locale.ROOT);
                                authorities.add(new SimpleGrantedAuthority("MODULE_" + moduleName));
                            }
                        }

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user, null, authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}