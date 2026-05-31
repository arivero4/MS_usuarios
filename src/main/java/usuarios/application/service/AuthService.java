package usuarios.application.service;

import org.springframework.stereotype.Service;
import usuarios.application.port.in.AuthUseCase;
import usuarios.application.port.out.PasswordEncoderPort;
import usuarios.application.port.out.TokenPort;
import usuarios.application.port.out.UsuarioRepositoryPort;
import usuarios.domain.exception.CredencialesInvalidasException;
import usuarios.domain.exception.UsuarioException;
import usuarios.domain.model.Usuario;

/**
 * Servicio de aplicación que implementa los casos de uso de autenticación JWT.
 *
 * <p>Orquesta el flujo completo de autenticación:</p>
 * <ol>
 *   <li>Busca al usuario por correo en el repositorio.</li>
 *   <li>Verifica la contraseña con BCrypt.</li>
 *   <li>Comprueba que el usuario esté activo y no bloqueado.</li>
 *   <li>Delega la generación del JWT al puerto {@link TokenPort}.</li>
 * </ol>
 *
 * <p>No conoce ninguna tecnología concreta (ni JDBC, ni BCrypt, ni JJWT);
 * solo trabaja con las abstracciones de los puertos de salida.</p>
 */
@Service
public class AuthService implements AuthUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    /** Puerto de salida para generación y validación de tokens JWT. */
    private final TokenPort tokenPort;
    /** Puerto de salida para comparar contraseñas con el hash BCrypt. */
    private final PasswordEncoderPort passwordEncoder;

    public AuthService(UsuarioRepositoryPort usuarioRepository,
                       TokenPort tokenPort,
                       PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenPort         = tokenPort;
        this.passwordEncoder   = passwordEncoder;
    }

    /**
     * Autentica al usuario con correo y contraseña.
     *
     * <p>El flujo es:</p>
     * <ol>
     *   <li>Busca el usuario; si no existe lanza {@link CredencialesInvalidasException}
     *       (mensaje genérico para no revelar si el correo existe).</li>
     *   <li>Compara la contraseña en texto plano con el hash BCrypt almacenado.
     *       Si no coincide lanza {@link CredencialesInvalidasException}.</li>
     *   <li>Si el usuario está {@code BLOQUEADO} lanza error {@code AUTH-002}.</li>
     *   <li>Si el usuario no está {@code ACTIVO} (suspendido o inactivo) lanza {@code AUTH-003}.</li>
     *   <li>Genera el JWT con los datos del usuario (id, nombre, estado, grupos).</li>
     * </ol>
     *
     * @param correo   Correo electrónico del usuario.
     * @param password Contraseña en texto plano ingresada en el formulario.
     * @return Token JWT firmado con HS256, válido durante 24 horas (configurable).
     * @throws CredencialesInvalidasException si correo o contraseña son incorrectos (AUTH-001).
     * @throws UsuarioException               si el usuario está bloqueado (AUTH-002) o inactivo (AUTH-003).
     */
    @Override
    public String autenticar(String correo, String password) {
        // Paso 1: buscar usuario; mensaje genérico para evitar enumeración:
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        // Paso 2: comparar contraseña con BCrypt:
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }

        // Paso 3: validar estado del usuario:
        if (usuario.estaBloqueado()) {
            throw new UsuarioException("AUTH-002", "Usuario bloqueado. Contacte al administrador.");
        }
        if (!usuario.estaActivo()) {
            throw new UsuarioException("AUTH-003", "Usuario inactivo.");
        }

        // Paso 4: generar y devolver el JWT:
        return tokenPort.generarToken(usuario);
    }

    /**
     * Verifica si un token JWT es válido (no expirado y firma correcta).
     * Delega completamente al adaptador {@link usuarios.infrastructure.adapter.out.security.JwtTokenAdapter}.
     *
     * @param token Token JWT a validar (sin prefijo "Bearer ").
     * @return {@code true} si el token es válido, {@code false} en caso contrario.
     */
    @Override
    public boolean validarToken(String token) {
        return tokenPort.validarToken(token);
    }

    /**
     * Renueva un token JWT válido generando uno nuevo con el mismo payload
     * pero con una nueva fecha de expiración.
     *
     * @param token Token JWT actual (no expirado).
     * @return Nuevo token JWT con expiración extendida.
     * @throws UsuarioException si el token es inválido o ya expiró (AUTH-004).
     */
    @Override
    public String renovarToken(String token) {
        if (!tokenPort.validarToken(token)) {
            throw new UsuarioException("AUTH-004", "Token inválido o expirado.");
        }
        return tokenPort.renovarToken(token);
    }
}
