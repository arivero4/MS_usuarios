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

public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenPort tokenPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public JwtAuthFilter(TokenPort tokenPort, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.tokenPort = tokenPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extraerToken(request);

        if (StringUtils.hasText(token) && tokenPort.validarToken(token)) {
            String correo = tokenPort.extraerCorreo(token);
            Optional<Usuario> usuarioOpt = usuarioRepositoryPort.buscarPorCorreo(correo);

            usuarioOpt.ifPresent(usuario -> {
                List<SimpleGrantedAuthority> authorities = usuario.getGrupos() != null
                        ? usuario.getGrupos().stream()
                            .map(g -> new SimpleGrantedAuthority("ROLE_" + g.getNombre().toUpperCase()))
                            .collect(Collectors.toList())
                        : List.of();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(correo, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
