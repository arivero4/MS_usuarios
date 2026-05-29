package usuarios.domain.exception;

public class UsuarioAlreadyExistsException extends UsuarioException {

    public UsuarioAlreadyExistsException(String correo) {
        super("USR-002", "Ya existe un usuario registrado con el correo: " + correo);
    }
}
