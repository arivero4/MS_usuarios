package usuarios.infrastructure.adapter.in.web;

import usuarios.domain.exception.CredencialesInvalidasException;
import usuarios.domain.exception.GrupoNotFoundException;
import usuarios.domain.exception.UsuarioAlreadyExistsException;
import usuarios.domain.exception.UsuarioException;
import usuarios.domain.exception.UsuarioNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(GrupoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGrupoNotFound(GrupoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(UsuarioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioException(UsuarioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            errores.put(campo, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERR-500", "Error interno del servidor"));
    }

    public static class ErrorResponse {
        private final String codigo;
        private final String mensaje;
        private final LocalDateTime timestamp;

        public ErrorResponse(String codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
            this.timestamp = LocalDateTime.now();
        }

        public String getCodigo() { return codigo; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
