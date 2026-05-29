package usuarios.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private Long expiracion;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public Long getExpiracion() { return expiracion; }
    public void setExpiracion(Long expiracion) { this.expiracion = expiracion; }
}
