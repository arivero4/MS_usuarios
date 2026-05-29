package usuarios.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Configura el JdbcTemplate con timeout para Oracle 10g.
 */
@Configuration
public class OracleJdbcConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource));
        jdbcTemplate.setQueryTimeout(30);
        return jdbcTemplate;
    }
}
