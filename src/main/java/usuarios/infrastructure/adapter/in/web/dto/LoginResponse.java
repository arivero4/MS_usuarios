package usuarios.infrastructure.adapter.in.web.dto;

/**
 * DTO de respuesta para los endpoints de autenticación:
 * {@code POST /api/v1/auth/login} y {@code POST /api/v1/auth/renovar}.
 *
 * <p>Ejemplo de JSON de salida:</p>
 * <pre>
 * {
 *   "token":     "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGVtcHJlc2EuY29tIiwiaWQiOjEsLi4ufQ.signedPart",
 *   "tipo":      "Bearer",
 *   "expiracion": 86400000
 * }
 * </pre>
 *
 * <p>El cliente debe incluir el token en peticiones posteriores como:
 * {@code Authorization: Bearer <token>}</p>
 */
public class LoginResponse {

    /**
     * Token JWT compacto firmado con HS256.
     * Formato: {@code base64(header).base64(payload).base64(signature)}.
     * El cliente debe almacenarlo de forma segura (no en localStorage por XSS).
     */
    private String token;

    /**
     * Esquema de autenticación HTTP. Siempre {@code "Bearer"}.
     * Indica cómo enviar el token en el header Authorization.
     */
    private String tipo;

    /**
     * Tiempo de vida del token en milisegundos desde la emisión.
     * Valor por defecto: 86.400.000 ms (= 24 horas), configurable en
     * {@code jwt.expiracion} de {@code application.yml}.
     * El cliente puede calcular la expiración absoluta: {@code Date.now() + expiracion}.
     */
    private Long expiracion;

    /**
     * Constructor que fija el tipo como "Bearer" automáticamente.
     *
     * @param token      JWT compacto generado por {@link usuarios.infrastructure.adapter.out.security.JwtTokenAdapter}.
     * @param expiracion Tiempo de vida en milisegundos leído de {@link usuarios.infrastructure.config.JwtConfig}.
     */
    public LoginResponse(String token, Long expiracion) {
        this.token = token;
        this.tipo = "Bearer"; // Siempre Bearer en este microservicio
        this.expiracion = expiracion;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public Long getExpiracion() { return expiracion; }
}
