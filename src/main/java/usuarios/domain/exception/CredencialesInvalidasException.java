package usuarios.domain.exception;

public class CredencialesInvalidasException extends UsuarioException {

    public CredencialesInvalidasException() {
        super("AUTH-001", "Correo o contraseña incorrectos");
    }
}
