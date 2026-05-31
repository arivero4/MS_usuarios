package usuarios.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import usuarios.application.port.out.TokenPort;
import usuarios.application.port.out.UsuarioRepositoryPort;
import usuarios.domain.model.Usuario;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Filtro HTTP que intercepta cada petición para extraer y validar el token JWT.
 *
 * <p>Extiende {@link OncePerRequestFilter}, que garantiza que el filtro
 * se ejecuta <strong>exactamente una vez por petición</strong>, independientemente
 * del número de dispatches internos de Spring MVC.</p>
 *
 * <p>Flujo de procesamiento:</p>
 * <ol>
 *   <li>Extrae el token del header {@code Authorization: Bearer <token>}.</li>
 *   <li>Si el token existe y es válido (firma y expiración), extrae el correo del subject.</li>
 *   <li>Carga el usuario completo desde la BD para obtener sus grupos actuales.</li>
 *   <li>Construye las authorities de Spring Security con el prefijo {@code ROLE_}
 *       a partir de los nombres de los grupos (ej: {@code ROLE_ADMINISTRADOR}).</li>
 *   <li>Registra la autenticación en el {@link SecurityContextHolder} para que
 *       el resto de la cadena de filtros y los controladores puedan acceder a ella.</li>
 *   <li>Continúa la cadena con {@code filterChain.doFilter()}.</li>
 * </ol>
 *
 * <p>Si el token no está presente o es inválido, el filtro simplemente pasa
 * la petición sin registrar autenticación. El {@code AuthorizationFilter} de
 * Spring Security rechazará la petición con 401 si el endpoint lo requiere.</p>
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Para validar el token y extraer el correo del subject. */
    private final TokenPort tokenPort;

    /** Para cargar el usuario y sus grupos actuales desde la BD. */
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public JwtAuthFilter(TokenPort tokenPort, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.tokenPort = tokenPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    /**
     * Lógica principal del filtro. Se ejecuta una vez por cada petición HTTP.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Paso 1: Extraer el token del header Authorization:
        String token = extraerToken(request);

        // Paso 2: Procesar solo si hay token y es válido:
        if (StringUtils.hasText(token) && tokenPort.validarToken(token)) {
            String correo = tokenPort.extraerCorreo(token); // Subject del JWT

            // Paso 3: Cargar usuario desde BD (para obtener grupos actuales, no los del token):
            Optional<Usuario> usuarioOpt = usuarioRepositoryPort.buscarPorCorreo(correo);

            usuarioOpt.ifPresent(usuario -> {
                // Paso 4: Construir authorities con formato ROLE_NOMBRE_GRUPO:
                List<SimpleGrantedAuthority> authorities = usuario.getGrupos() != null
                        ? usuario.getGrupos().stream()
                            .map(g -> new SimpleGrantedAuthority("ROLE_" + g.getNombre().toUpperCase()))
                            .collect(Collectors.toList())
                        : List.of();

                // Paso 5: Registrar la autenticación en el contexto de Spring Security:
                // credentials = null (no necesitamos la contraseña en peticiones autenticadas)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(correo, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                // A partir de aquí, el principal es el correo del usuario autenticado
            });
        }

        // Paso 6: Continuar la cadena de filtros independientemente del resultado:
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header {@code Authorization}.
     * El formato esperado es {@code Bearer <token>}.
     *
     * @param request Petición HTTP entrante.
     * @return El token sin el prefijo "Bearer ", o {@code null} si el header
     *         no está presente o no tiene el formato correcto.
     */
    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        // StringUtils.hasText verifica que no sea null, vacío ni solo espacios:
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // Elimina "Bearer " (7 caracteres)
        }
        return null;
    }
}
