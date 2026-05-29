package usuarios.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Configuración del DataSource para Oracle 10g.
 * Usa DriverManagerDataSource (sin pool) por compatibilidad con ojdbc14.
 */
@Configuration
public class OracleDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(Objects.requireNonNull(driverClassName, "driver-class-name requerido"));
        ds.setUrl(Objects.requireNonNull(url, "datasource url requerida"));
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }
}
