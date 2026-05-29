package usuarios.application.port.in;

public interface AuthUseCase {

    String autenticar(String correo, String password);

    boolean validarToken(String token);

    String renovarToken(String token);
}
