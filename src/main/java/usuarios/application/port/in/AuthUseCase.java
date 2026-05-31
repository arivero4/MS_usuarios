package usuarios.application.port.in;

/**
 * Puerto de entrada que define los casos de uso de autenticación JWT.
 *
 * <p>Gestiona el ciclo de vida completo de los tokens JWT:
 * emisión, validación y renovación.</p>
 *
 * <p>Implementado por: {@link usuarios.application.service.AuthService}.</p>
 *
 * <p>Endpoints asociados (todos bajo {@code /api/v1/auth}):</p>
 * <ul>
 *   <li>{@code POST /login}   → {@link #autenticar}</li>
 *   <li>{@code GET  /validar} → {@link #validarToken}</li>
 *   <li>{@code POST /renovar} → {@link #renovarToken}</li>
 * </ul>
 */
public interface AuthUseCase {

    /**
     * Autentica un usuario con correo y contraseña y emite un token JWT.
     *
     * <p>Flujo interno:</p>
     * <ol>
     *   <li>Busca el usuario por correo.</li>
     *   <li>Compara la contraseña con el hash BCrypt.</li>
     *   <li>Verifica que el usuario esté activo y no bloqueado.</li>
     *   <li>Genera el JWT con los datos del usuario (id, nombre, grupos).</li>
     * </ol>
     *
     * @param correo   Correo electrónico del usuario.
     * @param password Contraseña en texto plano.
     * @return Token JWT compacto válido por 24 horas (configurable en {@code jwt.expiracion}).
     * @throws usuarios.domain.exception.CredencialesInvalidasException si las credenciales son incorrectas (AUTH-001).
     * @throws usuarios.domain.exception.UsuarioException               si el usuario está bloqueado (AUTH-002) o inactivo (AUTH-003).
     */
    String autenticar(String correo, String password);

    /**
     * Verifica si un token JWT es válido (firma correcta y no expirado).
     * Usado por otros microservicios para validar tokens antes de procesar peticiones.
     *
     * @param token Token JWT sin prefijo "Bearer ".
     * @return {@code true} si el token es válido; {@code false} si expiró o es inválido.
     */
    boolean validarToken(String token);

    /**
     * Renueva un token JWT válido generando uno nuevo con nueva fecha de expiración.
     * Permite mantener sesiones activas sin pedir credenciales nuevamente.
     *
     * @param token Token JWT actual válido (no expirado).
     * @return Nuevo token JWT con expiración extendida.
     * @throws usuarios.domain.exception.UsuarioException si el token es inválido o ya expiró (AUTH-004).
     */
    String renovarToken(String token);
}
