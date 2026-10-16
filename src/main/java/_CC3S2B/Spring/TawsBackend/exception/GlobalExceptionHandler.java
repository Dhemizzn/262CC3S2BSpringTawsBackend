package _CC3S2B.Spring.TawsBackend.exception;

import _CC3S2B.Spring.TawsBackend.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice //Anotacion de Spring Boot que indica que esta clase se utilizara para maejar excepiones de Rest
public class GlobalExceptionHandler {
    //le indica a Spring qué excepción debe manejar este método
    @ExceptionHandler(BadCredentialsException.class)//Es una referencia al tipo de excepción BadCredentialsException, que pertenece a Spring Security.
    //Se utiliza cuando las credenciales proporcionadas no son válidas.
    public ResponseEntity<ErrorResponse> handleAuthentication(BadCredentialsException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Correo o contraseña incorrectos")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);//ResponseEntity es una clase que permite construir y controlar una respuesta HTTP completa
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage() != null ? ex.getMessage() : "Datos invalidos")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Error interno del servidor")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);//
    }
}
