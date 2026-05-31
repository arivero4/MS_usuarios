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

/**
 * Manejador centralizado de excepciones para todos los controladores REST.
 *
 * <p>Anotado con {@code @RestControllerAdvice}, intercepta las excepciones
 * no capturadas por los controladores y las traduce a respuestas HTTP con
 * el código de estado y cuerpo JSON apropiados.</p>
 *
 * <p>Tabla de mapeo excepción → HTTP:</p>
 * <pre>
 *  UsuarioNotFoundException       → 404 Not Found      (USR-001)
 *  GrupoNotFoundException         → 404 Not Found      (GRP-001)
 *  UsuarioAlreadyExistsException  → 409 Conflict       (USR-002)
 *  CredencialesInvalidasException → 401 Unauthorized   (AUTH-001)
 *  UsuarioException (resto)       → 400 Bad Request    (USR-003, AUTH-002, etc.)
 *  MethodArgumentNotValidException→ 400 Bad Request    (validaciones @NotBlank, @Email, etc.)
 *  Exception (catch-all)          → 500 Internal Error (ERR-500)
 * </pre>
 *
 * <p>El cuerpo de error tiene siempre la estructura:</p>
 * <pre>
 * {
 *   "codigo":    "USR-001",
 *   "mensaje":   "Usuario no encontrado con id: 5",
 *   "timestamp": "2026-05-30T10:00:00"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura búsquedas de usuario que no existen.
     * Responde con {@code 404 Not Found} y código {@code USR-001}.
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /**
     * Captura búsquedas de grupo que no existen.
     * Responde con {@code 404 Not Found} y código {@code GRP-001}.
     */
    @ExceptionHandler(GrupoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGrupoNotFound(GrupoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /**
     * Captura intentos de crear un usuario con correo duplicado.
     * Responde con {@code 409 Conflict} y código {@code USR-002}.
     */
    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(UsuarioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /**
     * Captura credenciales incorrectas o usuario no encontrado en el login.
     * Responde con {@code 401 Unauthorized} y código {@code AUTH-001}.
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /**
     * Captura cualquier {@link UsuarioException} no manejada por los handlers anteriores
     * (p. ej. USR-003 estado inválido, AUTH-002 usuario bloqueado, PRV-001 privilegio no encontrado).
     * Responde con {@code 400 Bad Request}.
     */
    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioException(UsuarioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    /**
     * Captura errores de validación de Bean Validation (Jakarta Validation).
     * Se activa cuando un DTO con anotaciones {@code @NotBlank}, {@code @Email},
     * {@code @Size}, etc., falla la validación en un endpoint {@code @Valid}.
     *
     * <p>Responde con {@code 400 Bad Request} y un mapa de campo → mensaje de error:
     * <pre>
     * {
     *   "correo":   "Formato de correo inválido",
     *   "password": "La contraseña debe tener al menos 8 caracteres"
     * }
     * </pre>
     * </p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        // Itera sobre todos los errores de campo y construye el mapa:
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            errores.put(campo, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    /**
     * Catch-all para cualquier excepción no controlada.
     * Responde con {@code 500 Internal Server Error} y código genérico {@code ERR-500}.
     * El mensaje real del error no se expone al cliente por seguridad.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERR-500", "Error interno del servidor"));
    }

    // ── Clase interna de respuesta de error ───────────────────────────────────

    /**
     * DTO de respuesta de error estandarizado para todos los errores HTTP.
     * Se serializa automáticamente como JSON por Jackson.
     */
    public static class ErrorResponse {
        /** Código único del error del dominio (ej: "USR-001", "AUTH-002"). */
        private final String codigo;
        /** Mensaje descriptivo del error, legible para el desarrollador cliente. */
        private final String mensaje;
        /** Timestamp de cuando ocurrió el error. Útil para correlacionar con logs. */
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
