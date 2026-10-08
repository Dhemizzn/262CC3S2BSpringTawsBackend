package _CC3S2B.Spring.TawsBackend.service;
import _CC3S2B.Spring.TawsBackend.model.User;
import io.jsonwebtoken.Claims; //Representa la información contenida dentro del JWT.
import io.jsonwebtoken.Jwts; // Es la clase principal de JJWT. La utilizas para Crear tokens,Firmarlos,Parsearlos,Validarlos
import io.jsonwebtoken.io.Decoders; // Sirve para decodificar la clave secreta que normalmente tienes almacenada como Base64.
import io.jsonwebtoken.security.Keys; // Sirve para crear una SecretKey a partir de los bytes de tu clave.
import org.springframework.beans.factory.annotation.Value; //Permite obtener valores desde la configuración de Spring.
import org.springframework.stereotype.Service; //Indica que tu clase es un servicio administrado por Spring.
import javax.crypto.SecretKey; // Representa la clave criptográfica utilizada para firmar/verificar el JWT.
import java.util.Date; // Se utiliza normalmente para controlar las fechas del JWT
import java.util.HashMap; //Es una implementación de Map
import java.util.List; //Representa una lista
import java.util.Map;
import java.util.function.Function; //función que recibe algo y devuelve algo

@Service
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("3600000")
    private long jwtExpiration;

    public String generateToken(User user, List<Module> modules){
        Map<String,Object> claims = new HashMap<>();
        claims.put("personId",user.getPerson().getPersonId());
        claims.put("email",user.getEmail());
        claims.put("names",user.getPerson().getFirstName() + user.getPerson().getMaternalSurname() + user.getPerson().getPaternalSurname());
        claims.put("rol",user.getRol());
        claims.put("modules",modules);

        return Jwts.builder().
                claims(claims).
                subject(user.getEmail()).
                issuedAt(new Date(System.currentTimeMillis())).
                expiration(new Date(System.currentTimeMillis() + jwtExpiration)).
                signWith(getSigninKey()).
                compact();
    }


    public SecretKey getSigninKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token){
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        final Claims claims = extractAllClaims(token);
        return extractClaim(token,Claims::getExpiration);
    }

    private <T> T extractClaim(String token,Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser() //Voy a preparar un lector Parser de JWT
                .verifyWith(getSigninKey()) // Cuando lea el JWT, quiero que verifique su firma usando esta clave.
                .build() // Contruye el parser
                .parseSignedClaims(token) // Toma este token, léelo y comprueba su firma. Si es valido, extrae sus claims
                .getPayload(); // Dame el payload, es decir, los claims que acabas de obtener.
    }
}
