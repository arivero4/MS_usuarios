package usuarios.domain.exception;

/**
 * Se lanza cuando se intenta crear un usuario con un correo ya registrado.
 *
 * <p>El {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}
 * la captura y responde con {@code HTTP 409 Conflict}.</p>
 *
 * <p>Código de error del dominio: {@code USR-002}.</p>
 *
 * <p>El control se realiza en
 * {@link usuarios.application.service.UsuarioService#crearUsuario},
 * que consulta {@code UsuarioRepositoryPort#existePorCorreo} antes de persistir.</p>
 */
public class UsuarioAlreadyExistsException extends UsuarioException {

    /**
     * @param correo Correo duplicado que provocó el conflicto.
     */
    public UsuarioAlreadyExistsException(String correo) {
        super("USR-002", "Ya existe un usuario registrado con el correo: " + correo);
    }
}
