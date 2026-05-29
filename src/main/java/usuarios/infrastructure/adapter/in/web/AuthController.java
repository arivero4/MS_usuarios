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

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints de autenticación JWT")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final JwtConfig jwtConfig;

    public AuthController(AuthUseCase authUseCase, JwtConfig jwtConfig) {
        this.authUseCase = authUseCase;
        this.jwtConfig = jwtConfig;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario y obtener token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authUseCase.autenticar(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token, jwtConfig.getExpiracion()));
    }

    @PostMapping("/renovar")
    @Operation(summary = "Renovar token JWT")
    public ResponseEntity<LoginResponse> renovar(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String nuevoToken = authUseCase.renovarToken(token);
        return ResponseEntity.ok(new LoginResponse(nuevoToken, jwtConfig.getExpiracion()));
    }

    @GetMapping("/validar")
    @Operation(summary = "Validar token JWT")
    public ResponseEntity<Boolean> validar(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authUseCase.validarToken(token));
    }
}
