package usuarios.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de beans de infraestructura transversales.
 * Los servicios de aplicación son detectados automáticamente via @Service.
 * Los adaptadores de repositorio son detectados via @Repository.
 */
@Configuration
public class PersistenceConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
