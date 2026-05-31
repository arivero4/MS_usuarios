package usuarios.domain.exception;

/**
 * Se lanza cuando el correo no existe o la contraseña no coincide durante
 * el proceso de autenticación.
 *
 * <p>El {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}
 * la captura y responde con {@code HTTP 401 Unauthorized}.</p>
 *
 * <p>Código de error del dominio: {@code AUTH-001}.</p>
 *
 * <p><strong>Importante de seguridad:</strong> el mensaje es genérico
 * ("Correo o contraseña incorrectos") para no revelar si el correo
 * existe o no en el sistema (previene enumeración de usuarios).</p>
 *
 * <p>Lanzada en {@link usuarios.application.service.AuthService#autenticar}
 * en dos casos:
 * <ol>
 *   <li>El correo no está registrado ({@code buscarPorCorreo} devuelve vacío).</li>
 *   <li>La contraseña no coincide con el hash BCrypt almacenado.</li>
 * </ol>
 * </p>
 */
public class CredencialesInvalidasException extends UsuarioException {

    /** Constructor sin parámetros: el mensaje es siempre genérico por seguridad. */
    public CredencialesInvalidasException() {
        super("AUTH-001", "Correo o contraseña incorrectos");
    }
}
