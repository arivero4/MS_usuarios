package usuarios.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración que vincula las propiedades del prefijo {@code jwt}
 * del archivo {@code application.yml} a campos Java.
 *
 * <p>La anotación {@code @ConfigurationProperties(prefix = "jwt")} instruye a Spring Boot
 * para inyectar automáticamente las propiedades:</p>
 * <pre>
 * jwt:
 *   secret:    "clave-secreta-de-minimo-32-caracteres"  → getSecret()
 *   expiracion: 86400000                                 → getExpiracion()
 * </pre>
 *
 * <p>Ambas propiedades son sobreescribibles por variables de entorno:
 * {@code JWT_SECRET} y {@code JWT_EXPIRACION} respectivamente.</p>
 *
 * <p>Usada por:</p>
 * <ul>
 *   <li>{@link JwtTokenAdapter} – para firmar y verificar tokens.</li>
 *   <li>{@link usuarios.infrastructure.adapter.in.web.AuthController} – para incluir
 *       el tiempo de expiración en la respuesta del login.</li>
 * </ul>
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * Clave secreta para firmar tokens JWT con HMAC-SHA256.
     * Debe tener <strong>mínimo 32 caracteres</strong> (256 bits) para que JJWT
     * acepte la clave para el algoritmo HS256.
     * En producción se inyecta desde la variable de entorno {@code JWT_SECRET}.
     */
    private String secret;

    /**
     * Tiempo de vida del token en milisegundos.
     * Valor por defecto: {@code 86400000} (= 86.400 seg = 24 horas).
     * En producción se puede ajustar con la variable {@code JWT_EXPIRACION}.
     */
    private Long expiracion;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public Long getExpiracion() { return expiracion; }
    public void setExpiracion(Long expiracion) { this.expiracion = expiracion; }
}
