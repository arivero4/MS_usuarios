package usuarios.application.port.out;

/**
 * Puerto de salida que abstrae el mecanismo de cifrado de contraseñas.
 *
 * <p>Permite que la capa de aplicación cifre y compare contraseñas sin conocer
 * que la implementación usa BCrypt. Si se cambia el algoritmo de hash (por ejemplo
 * a Argon2), solo cambia el adaptador, no el servicio de aplicación.</p>
 *
 * <p>Implementado por:
 * {@link usuarios.infrastructure.adapter.out.encoder.BcryptPasswordEncoderAdapter}
 * que delega en {@code BCryptPasswordEncoder} de Spring Security.</p>
 *
 * <p>BCrypt genera un salt aleatorio en cada cifrado, por lo que dos llamadas
 * a {@code encode} con la misma contraseña producen hashes diferentes.
 * La comparación siempre se hace con {@code matches}, nunca comparando strings.</p>
 */
public interface PasswordEncoderPort {

    /**
     * Cifra una contraseña en texto plano con BCrypt.
     * BCrypt incluye el salt dentro del hash resultante, por lo que no es
     * necesario almacenarlo por separado.
     *
     * @param rawPassword Contraseña en texto plano ingresada por el usuario.
     * @return Hash BCrypt de 60 caracteres listo para almacenar en BD.
     */
    String encode(String rawPassword);

    /**
     * Compara una contraseña en texto plano con su hash BCrypt almacenado.
     * Extrae el salt del hash para realizar la comparación correctamente.
     *
     * @param rawPassword     Contraseña en texto plano ingresada al hacer login.
     * @param encodedPassword Hash BCrypt almacenado en la tabla {@code USUARIO}.
     * @return {@code true} si la contraseña corresponde al hash; {@code false} si no.
     */
    boolean matches(String rawPassword, String encodedPassword);
}
