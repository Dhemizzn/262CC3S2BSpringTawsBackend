package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.AuthResponseDTO;
import _CC3S2B.Spring.TawsBackend.dto.LoginRequestDTO;
import _CC3S2B.Spring.TawsBackend.dto.RegisterRequestDTO;
import _CC3S2B.Spring.TawsBackend.model.Person;
import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.repository.ModuleRepository;
import _CC3S2B.Spring.TawsBackend.repository.PersonRepository;
import _CC3S2B.Spring.TawsBackend.repository.RolRepository;
import _CC3S2B.Spring.TawsBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Genera un constructor para los atributos que son final
public class AuthService {
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ModuleRepository moduleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;
    private final RolRepository rolRepository;


    public AuthResponseDTO login(LoginRequestDTO request){
        if(!userRepository.existsByEmail(request.getUsername())){
            throw new IllegalArgumentException("Email no encontrado");
        }
        var user = userRepository.findByEmail(request.getUsername().trim()).orElseThrow();
        if(!passwordEncoder.matches(request.getPassword(),user.getPasswordHash()))
            throw new BadCredentialsException("Contrasena incorrecta");

        List<Module> modules = Collections.emptyList();
        if(user.getRol()!=null){
            modules = moduleRepository.findByRoleId(user.getRol().getRolId());
        }
        var token = jwtService.generateToken(user,modules);
        return AuthResponseDTO.builder().token(token).build();
    }

    public AuthResponseDTO register(RegisterRequestDTO request){
        if(userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Correo ya registrado");
        if(personRepository.existsByNumDocumento(request.getDni()))
            throw new IllegalArgumentException("Dni ya registrado");
        Person person = Person.builder()
                .firstName(request.getFirstName())
                .paternalSurname(request.getPaternalSurname())
                .maternalSurname(request.getMaternalSurname())
                .dni(request.getDni())
                .phone(request.getPhone())
                .direction(request.getDirection())
                .email(request.getEmail())
                .build();
        personRepository.save(person);

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(generatePassword()))
                .person(person)
                .rol(rolRepository.findbyId(1))
                .build();
        userRepository.save(user);

        var token = jwtService.generateToken(user,Collections.emptyList());
        return AuthResponseDTO.builder().token(token).build();
    }

    private String generatePassword() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 12);
    }

}
