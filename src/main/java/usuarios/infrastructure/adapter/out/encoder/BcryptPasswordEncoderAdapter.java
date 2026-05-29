package usuarios.infrastructure.adapter.out.encoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import usuarios.application.port.out.PasswordEncoderPort;

/**
 * Adaptador de salida: implementa PasswordEncoderPort usando BCrypt de Spring Security.
 * La capa de aplicación desconoce BCrypt; solo conoce el puerto.
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
