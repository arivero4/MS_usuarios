package usuarios.domain.exception;

/**
 * Excepción base del dominio de usuarios.
 *
 * <p>Todas las excepciones de negocio del microservicio extienden esta clase,
 * lo que permite al {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}
 * capturarlas de forma centralizada y traducirlas a respuestas HTTP con
 * código de error y mensaje apropiados.</p>
 *
 * <p>Extiende {@link RuntimeException} (unchecked) para que los servicios
 * de aplicación no necesiten declarar {@code throws} en sus firmas,
 * manteniendo limpia la interfaz de los puertos.</p>
 *
 * <p>Jerarquía de excepciones:</p>
 * <pre>
 *   UsuarioException (base)
 *   ├── UsuarioNotFoundException      → HTTP 404 (USR-001)
 *   ├── UsuarioAlreadyExistsException → HTTP 409 (USR-002)
 *   ├── GrupoNotFoundException        → HTTP 404 (GRP-001)
 *   └── CredencialesInvalidasException→ HTTP 401 (AUTH-001)
 * </pre>
 */
public class UsuarioException extends RuntimeException {

    /**
     * Código único de error del dominio.
     * Permite al cliente identificar el tipo de error de forma programática
     * sin depender del mensaje (que puede internacionalizarse).
     * Convención: {@code PREFIJO-NNN} (ej: {@code USR-001}, {@code AUTH-002}).
     */
    private final String codigo;

    /**
     * Constructor principal.
     *
     * @param codigo  Código de error del dominio (p. ej. {@code "USR-001"}).
     * @param mensaje Descripción legible del error.
     */
    public UsuarioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    /**
     * Constructor con causa. Útil para envolver excepciones de infraestructura
     * (JDBC, JWT) y relanzarlas como errores de dominio.
     *
     * @param codigo  Código de error del dominio.
     * @param mensaje Descripción legible del error.
     * @param causa   Excepción original que provocó este error.
     */
    public UsuarioException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    /**
     * @return El código de error del dominio (p. ej. {@code "USR-001"}).
     *         Incluido en la respuesta JSON por {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}.
     */
    public String getCodigo() {
        return codigo;
    }
}
