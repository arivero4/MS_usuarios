package usuarios.infrastructure.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import usuarios.application.port.out.TokenPort;
import usuarios.application.port.out.UsuarioRepositoryPort;

import java.util.List;

/**
 * Configuración central de Spring Security para el microservicio ms_usuarios.
 *
 * <p>Implementa un modelo de seguridad <strong>stateless</strong> (sin sesiones):
 * cada petición debe incluir un token JWT válido en el header
 * {@code Authorization: Bearer <token>}, excepto los endpoints públicos.</p>
 *
 * <p>Cadena de filtros aplicada a cada petición HTTP:</p>
 * <pre>
 *  Request → [CorsFilter] → [JwtAuthFilter] → [UsernamePasswordAuthenticationFilter]
 *         → [AuthorizationFilter] → Controller
 * </pre>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Puerto de salida para validar tokens JWT (implementado por JwtTokenAdapter). */
    private final TokenPort tokenPort;

    /** Puerto de salida para cargar el usuario autenticado desde la BD. */
    private final UsuarioRepositoryPort usuarioRepository;

    public SecurityConfig(TokenPort tokenPort, UsuarioRepositoryPort usuarioRepository) {
        this.tokenPort        = tokenPort;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     *
     * <p>Configuración aplicada:</p>
     * <ul>
     *   <li><strong>CORS</strong>: habilitado con orígenes permitidos ({@code localhost:*},
     *       {@code file://*} para desarrollo desde HTML local).</li>
     *   <li><strong>CSRF</strong>: deshabilitado porque la API es stateless (JWT).</li>
     *   <li><strong>Sesiones</strong>: {@code STATELESS} — Spring Security no crea ni usa
     *       {@code HttpSession}; la autenticación se verifica en cada petición vía JWT.</li>
     *   <li><strong>Endpoints públicos</strong>:
     *     <ul>
     *       <li>{@code POST /api/v1/auth/login} — autenticación inicial.</li>
     *       <li>{@code OPTIONS /**} — preflight CORS.</li>
     *       <li>{@code /v3/api-docs/**}, {@code /swagger-ui/**} — documentación API.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Resto de endpoints</strong>: requieren token JWT válido.</li>
     *   <li><strong>JwtAuthFilter</strong>: se inserta antes del filtro estándar de
     *       autenticación por usuario/contraseña para interceptar el JWT.</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configura CORS usando el bean corsConfigurationSource():
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Desactiva CSRF (innecesario en APIs stateless con JWT):
            .csrf(AbstractHttpConfigurer::disable)

            // No crear ni usar sesiones HTTP:
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Responde 401 (no redirige a /login) cuando falta autenticación:
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado")))

            // Reglas de autorización por endpoint:
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()  // Login público
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()               // CORS preflight
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Docs
                .anyRequest().authenticated())                                        // Todo lo demás requiere JWT

            // Inserta JwtAuthFilter antes del filtro estándar de autenticación:
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura los orígenes CORS permitidos.
     *
     * <p>Permite peticiones desde:</p>
     * <ul>
     *   <li>{@code http://localhost:*} — desarrollo local (cualquier puerto).</li>
     *   <li>{@code http://127.0.0.1:*} — equivalente a localhost.</li>
     *   <li>{@code null} — peticiones desde archivos HTML abiertos directamente
     *       ({@code file://}) sin servidor web (útil en pruebas del frontend local).</li>
     *   <li>{@code file://*} — origen explícito de archivos locales.</li>
     * </ul>
     *
     * <p>Se permiten todos los métodos HTTP y headers para simplificar el desarrollo.
     * En producción esto debería restringirse.</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "null", "file://*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*")); // Permite cualquier header (incluyendo Authorization)
        config.setAllowCredentials(true);       // Necesario para enviar cookies o headers de autenticación

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Aplica a todas las rutas
        return source;
    }

    /**
     * Crea el filtro JWT como Bean de Spring para que pueda inyectarse en la cadena.
     * Recibe los puertos necesarios para validar tokens y cargar usuarios.
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(tokenPort, usuarioRepository);
    }
}
