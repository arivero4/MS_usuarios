package usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio ms-usuarios.
 *
 * <p>La anotación {@code @SpringBootApplication} equivale a la combinación de:</p>
 * <ul>
 *   <li>{@code @Configuration}       – marca esta clase como fuente de beans Spring.</li>
 *   <li>{@code @EnableAutoConfiguration} – activa la autoconfiguración de Spring Boot
 *       (detecta librerías en el classpath y configura beans automáticamente, como
 *       {@code JdbcTemplate}, {@code PasswordEncoder}, Swagger, etc.).</li>
 *   <li>{@code @ComponentScan}       – escanea el paquete {@code usuarios} y
 *       subpaquetes buscando {@code @Component}, {@code @Service}, {@code @Repository},
 *       {@code @RestController}, etc.</li>
 * </ul>
 *
 * <p>Al arrancar, Spring Boot:</p>
 * <ol>
 *   <li>Lee {@code application.yml} (y {@code application-dev.yml} si el perfil es {@code dev}).</li>
 *   <li>Configura el {@code DataSource} para Oracle 10g con el driver {@code ojdbc14}.</li>
 *   <li>Crea el {@code JdbcTemplate} disponible para los repositorios.</li>
 *   <li>Configura Spring Security con la cadena definida en {@link usuarios.infrastructure.config.SecurityConfig}.</li>
 *   <li>Levanta el servidor Tomcat embebido en el puerto 8081 (configurable).</li>
 *   <li>Registra todos los endpoints REST en los controladores.</li>
 *   <li>Publica la documentación Swagger en {@code /swagger-ui.html}.</li>
 * </ol>
 *
 * <p>Comando para arrancar en perfil de desarrollo:</p>
 * <pre>
 *   java -jar -Dspring.profiles.active=dev ms-usuarios-1.0.0-SNAPSHOT.jar
 * </pre>
 */
@SpringBootApplication
public class MsUsuariosApplication {

    /**
     * Método {@code main} que inicia el contexto de Spring Boot.
     * {@code SpringApplication.run()} crea el {@code ApplicationContext},
     * inyecta todas las dependencias y arranca el servidor embebido.
     *
     * @param args Argumentos de línea de comandos (pueden sobreescribir propiedades de {@code yml}).
     */
    public static void main(String[] args) {
        SpringApplication.run(MsUsuariosApplication.class, args);
    }
}
