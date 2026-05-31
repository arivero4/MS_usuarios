package usuarios.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import usuarios.application.port.in.AuthUseCase;
import usuarios.infrastructure.adapter.in.web.dto.LoginRequest;
import usuarios.infrastructure.adapter.in.web.dto.LoginResponse;
import usuarios.infrastructure.config.JwtConfig;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controlador REST para los endpoints de autenticación JWT.
 *
 * <p>Adaptador de entrada de la arquitectura hexagonal: traduce peticiones HTTP
 * a llamadas al puerto de entrada {@link AuthUseCase} y las respuestas de vuelta
 * a JSON. No contiene lógica de negocio.</p>
 *
 * <p>Base URL: {@code /api/v1/auth}</p>
 *
 * <p>Endpoints:</p>
 * <pre>
 *  POST /api/v1/auth/login   → Autentica y devuelve JWT       [PÚBLICO]
 *  POST /api/v1/auth/renovar → Renueva el JWT actual          [AUTENTICADO]
 *  GET  /api/v1/auth/validar → Valida si el JWT es correcto   [AUTENTICADO]
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints de autenticación JWT")
public class AuthController {

    private final AuthUseCase authUseCase;
    /** Inyectado para incluir el tiempo de expiración en la respuesta del login. */
    private final JwtConfig jwtConfig;

    public AuthController(AuthUseCase authUseCase, JwtConfig jwtConfig) {
        this.authUseCase = authUseCase;
        this.jwtConfig = jwtConfig;
    }

    /**
     * Autentica al usuario con correo y contraseña.
     *
     * <p>Endpoint público (no requiere JWT). Configurado así en
     * {@link usuarios.infrastructure.config.SecurityConfig#filterChain}.</p>
     *
     * <p>Request body (JSON):</p>
     * <pre>
     * {
     *   "correo":   "admin@empresa.com",
     *   "password": "Admin1234!"
     * }
     * </pre>
     *
     * <p>Response body (JSON) {@code 200 OK}:</p>
     * <pre>
     * {
     *   "token":     "eyJhbGciOiJIUzI1NiJ9...",
     *   "tipo":      "Bearer",
     *   "expiracion": 86400000
     * }
     * </pre>
     *
     * <p>Errores posibles:</p>
     * <ul>
     *   <li>{@code 401 Unauthorized} – credenciales incorrectas (AUTH-001).</li>
     *   <li>{@code 400 Bad Request}  – usuario bloqueado (AUTH-002) o inactivo (AUTH-003).</li>
     *   <li>{@code 400 Bad Request}  – validación fallida (correo o contraseña vacíos).</li>
     * </ul>
     *
     * @param request DTO validado con {@code @Valid}: correo y contraseña requeridos.
     * @return {@link LoginResponse} con el JWT y su tiempo de expiración en milisegundos.
     */
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario y obtener token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authUseCase.autenticar(request.getCorreo(), request.getPassword());
        // Incluye la expiración de JwtConfig en la respuesta para que el cliente
        // sepa cuándo debe renovar el token:
        return ResponseEntity.ok(new LoginResponse(token, jwtConfig.getExpiracion()));
    }

    /**
     * Renueva un token JWT vigente generando uno nuevo con expiración extendida.
     *
     * <p>Requiere el token actual en el header {@code Authorization: Bearer <token>}.</p>
     *
     * <p>Response body (JSON) {@code 200 OK}: misma estructura que {@code /login}.</p>
     *
     * <p>Errores posibles:</p>
     * <ul>
     *   <li>{@code 400 Bad Request} – token inválido o expirado (AUTH-004).</li>
     * </ul>
     *
     * @param authHeader Header completo incluyendo "Bearer ". El prefijo se elimina antes de procesar.
     * @return {@link LoginResponse} con el nuevo JWT.
     */
    @PostMapping("/renovar")
    @Operation(summary = "Renovar token JWT")
    public ResponseEntity<LoginResponse> renovar(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", ""); // Extrae solo el token
        String nuevoToken = authUseCase.renovarToken(token);
        return ResponseEntity.ok(new LoginResponse(nuevoToken, jwtConfig.getExpiracion()));
    }

    /**
     * Valida si un token JWT es válido (firma correcta y no expirado).
     *
     * <p>Útil para que otros servicios del ecosistema verifiquen tokens
     * sin necesidad de conocer el secreto JWT.</p>
     *
     * <p>Response body: {@code true} o {@code false}.</p>
     *
     * @param authHeader Header completo incluyendo "Bearer ".
     * @return {@code 200 OK} con {@code true} si el token es válido, {@code false} si no.
     */
    @GetMapping("/validar")
    @Operation(summary = "Validar token JWT")
    public ResponseEntity<Boolean> validar(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authUseCase.validarToken(token));
    }
}
