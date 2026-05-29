package usuarios.infrastructure.adapter.out.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import usuarios.application.port.out.TokenPort;
import usuarios.domain.model.Usuario;
import usuarios.infrastructure.config.JwtConfig;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final JwtConfig jwtConfig;

    public JwtTokenAdapter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usuario.getId());
        claims.put("nombre", usuario.getNombre());
        claims.put("estado", usuario.getEstado() != null ? usuario.getEstado().name() : null);

        if (usuario.getGrupos() != null) {
            claims.put("grupos", usuario.getGrupos().stream()
                    .map(g -> g.getNombre())
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getCorreo())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiracion()))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String extraerCorreo(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public String renovarToken(String token) {
        Claims claims = getClaims(token);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiracion()))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
