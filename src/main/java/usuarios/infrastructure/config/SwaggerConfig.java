package usuarios.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la documentación OpenAPI 3 (Swagger UI) para el microservicio.
 *
 * <p>SpringDoc ({@code springdoc-openapi-starter-webmvc-ui 2.5.0}) genera automáticamente
 * el esquema OpenAPI leyendo las anotaciones {@code @Tag}, {@code @Operation} y
 * {@code @RestController} de los controladores. Esta clase complementa esa
 * generación automática con metadatos globales y la configuración de seguridad JWT.</p>
 *
 * <p>URLs de acceso (requieren que el servicio esté corriendo):</p>
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8081/swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code http://localhost:8081/v3/api-docs}</li>
 * </ul>
 *
 * <p>Ambas rutas están configuradas como públicas en
 * {@link SecurityConfig#filterChain} para acceso sin autenticación.</p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Define el bean {@link OpenAPI} que configura la documentación Swagger.
     *
     * <p>Se configura un esquema de seguridad {@code bearerAuth} que agrega
     * un campo de input "Authorize" en la UI de Swagger donde se puede ingresar
     * el token JWT para probar los endpoints protegidos directamente desde el navegador.</p>
     */
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth"; // Nombre del esquema JWT en Swagger UI

        return new OpenAPI()
                // Metadatos de la API que aparecen en la cabecera de Swagger UI:
                .info(new Info()
                        .title("MS Usuarios API")
                        .description("Microservicio de gestión de usuarios, grupos y privilegios")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de desarrollo")
                                .email("desarrollo@empresa.com")))

                // Aplica el esquema de seguridad JWT a todos los endpoints por defecto:
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // Define el esquema de seguridad Bearer JWT:
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP) // HTTP auth scheme
                                        .scheme("bearer")               // Bearer token
                                        .bearerFormat("JWT")));         // Formato informativo
    }
}
