package usuarios.domain.exception;

/**
 * Se lanza cuando no existe un usuario con el identificador solicitado.
 *
 * <p>El {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}
 * la captura y responde con {@code HTTP 404 Not Found}.</p>
 *
 * <p>Código de error del dominio: {@code USR-001}.</p>
 *
 * <p>Casos de uso donde se lanza:</p>
 * <ul>
 *   <li>{@link usuarios.application.service.UsuarioService#obtenerUsuarioPorId} – búsqueda por ID.</li>
 *   <li>{@link usuarios.application.service.UsuarioService#obtenerUsuarioPorCorreo} – búsqueda por correo.</li>
 *   <li>{@link usuarios.application.service.AuthService#autenticar} – usuario no registrado.</li>
 * </ul>
 */
public class UsuarioNotFoundException extends UsuarioException {

    /**
     * Constructor para búsqueda por ID numérico.
     *
     * @param id ID del usuario no encontrado.
     */
    public UsuarioNotFoundException(Long id) {
        super("USR-001", "Usuario no encontrado con id: " + id);
    }

    /**
     * Constructor para búsqueda por correo electrónico.
     * Usado en el flujo de autenticación y en la búsqueda por correo.
     *
     * @param correo Correo del usuario no encontrado.
     */
    public UsuarioNotFoundException(String correo) {
        super("USR-001", "Usuario no encontrado con correo: " + correo);
    }
}
