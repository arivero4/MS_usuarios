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

/**
 * Adaptador de salida que implementa {@link TokenPort} usando la librería JJWT 0.11.5.
 *
 * <p>Genera tokens JWT firmados con HMAC-SHA256 (HS256) usando la clave secreta
 * configurada en {@link JwtConfig} (mínimo 32 caracteres).</p>
 *
 * <p>Estructura del token generado:</p>
 * <pre>
 * Header:  { "alg": "HS256", "typ": "JWT" }
 * Payload: {
 *   "sub":    "correo@ejemplo.com",   // Subject = correo del usuario
 *   "id":     1,                      // ID numérico del usuario
 *   "nombre": "Nombre Apellido",      // Nombre completo
 *   "estado": "ACTIVO",               // Estado del usuario
 *   "grupos": ["ADMINISTRADOR"],      // Lista de nombres de grupos (roles)
 *   "iat":    1700000000,             // Issued-at (Unix timestamp)
 *   "exp":    1700086400              // Expiration (iat + 86400 seg = 24 h)
 * }
 * Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
 * </pre>
 *
 * <p>El claim {@code "grupos"} es consumido por los demás microservicios del ecosistema
 * para autorizar peticiones sin consultar la BD de usuarios.</p>
 */
@Component
public class JwtTokenAdapter implements TokenPort {

    private final JwtConfig jwtConfig;

    public JwtTokenAdapter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * Genera un token JWT firmado con los datos del usuario.
     *
     * @param usuario Usuario autenticado. Sus grupos se incluyen como lista de nombres.
     * @return Token JWT compacto en formato {@code header.payload.signature}.
     */
    @Override
    public String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        // Claims personalizados incluidos en el payload:
        claims.put("id", usuario.getId());
        claims.put("nombre", usuario.getNombre());
        claims.put("estado", usuario.getEstado() != null ? usuario.getEstado().name() : null);

        if (usuario.getGrupos() != null) {
            // Solo los nombres de los grupos para mantener el token compacto:
            claims.put("grupos", usuario.getGrupos().stream()
                    .map(g -> g.getNombre())
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .setClaims(claims)                         // Claims personalizados
                .setSubject(usuario.getCorreo())           // Subject estándar JWT = correo
                .setIssuedAt(new Date())                   // Fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiracion())) // Expiración
                .signWith(getKey(), SignatureAlgorithm.HS256) // Firma HMAC-SHA256
                .compact();                                // Construye el string JWT
    }

    /**
     * Valida que el token JWT tenga firma correcta y no esté expirado.
     *
     * @param token Token JWT sin prefijo "Bearer ".
     * @return {@code true} si el token es válido; {@code false} si está expirado,
     *         mal firmado o es nulo.
     */
    @Override
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey()) // Verifica la firma
                    .build()
                    .parseClaimsJws(token);  // Lanza excepción si es inválido
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException cubre: ExpiredJwtException, MalformedJwtException,
            // UnsupportedJwtException, SignatureException
            return false;
        }
    }

    /**
     * Extrae el correo del usuario (subject del JWT) sin requerir la clave secreta
     * para descodificar el payload (solo se verifica la firma en validarToken).
     *
     * @param token Token JWT válido.
     * @return Correo electrónico del usuario propietario del token.
     */
    @Override
    public String extraerCorreo(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Genera un nuevo token con el mismo payload pero con una nueva fecha
     * de emisión y expiración. Útil para mantener sesiones activas.
     *
     * @param token Token JWT actual válido.
     * @return Nuevo token JWT con expiración extendida.
     */
    @Override
    public String renovarToken(String token) {
        Claims claims = getClaims(token); // Extrae todos los claims del token actual
        return Jwts.builder()
                .setClaims(claims)                         // Reutiliza el mismo payload
                .setIssuedAt(new Date())                   // Nueva fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiracion()))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parsea el token y extrae todos sus claims.
     * Lanza excepción si el token es inválido o expirado.
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody(); // Retorna el payload como objeto Claims (Map)
    }

    /**
     * Construye la clave HMAC a partir del secreto configurado en {@link JwtConfig}.
     * JJWT requiere que la clave tenga al menos 32 bytes para HS256.
     */
    private Key getKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
