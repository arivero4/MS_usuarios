package usuarios.application.port.out;

import usuarios.domain.model.Usuario;

/**
 * Puerto de salida que abstrae la generación y validación de tokens de autenticación.
 *
 * <p>La capa de aplicación ({@link usuarios.application.service.AuthService}) usa esta
 * interfaz sin conocer que la implementación usa JWT + JJWT + HMAC-SHA256.
 * Si en el futuro se cambia a OAuth2 o PASETO, solo cambia el adaptador.</p>
 *
 * <p>Implementado por:
 * {@link usuarios.infrastructure.adapter.out.security.JwtTokenAdapter}.</p>
 */
public interface TokenPort {

    /**
     * Genera un token de autenticación para el usuario dado.
     * El token incluye como claims: id, nombre, estado, lista de grupos y correo (subject).
     *
     * @param usuario Usuario autenticado con sus grupos cargados.
     * @return Token compacto firmado con HS256. Formato: {@code header.payload.signature}.
     */
    String generarToken(Usuario usuario);

    /**
     * Verifica que el token sea auténtico (firma válida) y no haya expirado.
     *
     * @param token Token sin prefijo "Bearer ".
     * @return {@code true} si el token es válido y vigente; {@code false} en caso contrario.
     */
    boolean validarToken(String token);

    /**
     * Extrae el correo electrónico del usuario del subject del token.
     * No valida la firma; debe llamarse después de {@link #validarToken}.
     *
     * @param token Token válido.
     * @return Correo electrónico (subject del JWT).
     */
    String extraerCorreo(String token);

    /**
     * Genera un nuevo token con el mismo payload pero nueva fecha de expiración.
     * Útil para implementar "refresh" de sesión sin pedir credenciales nuevamente.
     *
     * @param token Token actual válido.
     * @return Nuevo token con expiración extendida.
     */
    String renovarToken(String token);
}
