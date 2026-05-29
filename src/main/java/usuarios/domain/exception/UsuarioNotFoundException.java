package usuarios.domain.exception;

public class UsuarioNotFoundException extends UsuarioException {

    public UsuarioNotFoundException(Long id) {
        super("USR-001", "Usuario no encontrado con id: " + id);
    }

    public UsuarioNotFoundException(String correo) {
        super("USR-001", "Usuario no encontrado con correo: " + correo);
    }
}
