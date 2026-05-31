package usuarios.infrastructure.adapter.out.encoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import usuarios.application.port.out.PasswordEncoderPort;

/**
 * Adaptador de salida que implementa {@link PasswordEncoderPort} usando
 * BCrypt de Spring Security.
 *
 * <p>BCrypt es un algoritmo de hash adaptativo diseñado para contraseñas:
 * <ul>
 *   <li>Incluye un <strong>salt aleatorio</strong> en cada hash, evitando ataques de tabla arcoíris.</li>
 *   <li>El <strong>factor de trabajo</strong> (cost factor) se puede aumentar a medida que
 *       el hardware mejora, sin cambiar las contraseñas existentes.</li>
 *   <li>Genera hashes de <strong>60 caracteres</strong> con formato:
 *       {@code $2a$10$saltSalt...hashHash...}</li>
 * </ul>
 * </p>
 *
 * <p>El bean {@code PasswordEncoder} es definido en {@link usuarios.infrastructure.config.SecurityConfig}
 * (via {@code @Bean} implícito de Spring Boot Security) y se inyecta aquí por constructor.</p>
 *
 * <p>Principio de Inversión de Dependencias: la capa de aplicación usa el puerto
 * {@link PasswordEncoderPort} sin saber que la implementación es BCrypt. Si se cambia
 * a Argon2, solo cambia este adaptador.</p>
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    /** Implementación concreta de BCrypt provista por Spring Security. */
    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cifra una contraseña en texto plano con BCrypt (factor de trabajo 10 por defecto).
     * Dos llamadas con la misma contraseña producen hashes distintos por el salt aleatorio.
     *
     * @param rawPassword Contraseña en texto plano.
     * @return Hash BCrypt de 60 caracteres listo para almacenar en la tabla {@code USUARIO}.
     */
    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Compara la contraseña en texto plano con el hash almacenado.
     * Extrae el salt del hash BCrypt para hacer la comparación de forma segura.
     *
     * @param rawPassword     Contraseña ingresada al hacer login.
     * @param encodedPassword Hash BCrypt almacenado en BD (60 caracteres).
     * @return {@code true} si coinciden; {@code false} si no.
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
