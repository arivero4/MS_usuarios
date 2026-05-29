package usuarios.application.port.out;

/**
 * Puerto de salida que abstrae el mecanismo de cifrado de contraseñas.
 * La aplicación lo define; la infraestructura (BCrypt) lo implementa.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
