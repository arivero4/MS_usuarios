package usuarios.domain.enums;

/**
 * Enumeración que representa los posibles estados del ciclo de vida
 * de un {@link usuarios.domain.model.Usuario} o un {@link usuarios.domain.model.Grupo}.
 *
 * <p>Se almacena en Oracle como {@code VARCHAR2(20)} con un CHECK constraint
 * que garantiza que solo se persistan estos cuatro valores.</p>
 *
 * <p>Flujo típico de transición:</p>
 * <pre>
 *   [Creación] → ACTIVO → SUSPENDIDO → ACTIVO   (suspensión temporal)
 *                       → BLOQUEADO              (bloqueo permanente por seguridad)
 *                       → INACTIVO               (baja definitiva)
 * </pre>
 */
public enum Estado {

    /**
     * El usuario o grupo está operativo y tiene acceso normal al sistema.
     * Es el estado inicial al momento de la creación.
     * Método de dominio relacionado: {@code Usuario#activar()}.
     */
    ACTIVO,

    /**
     * El usuario o grupo fue dado de baja. No puede autenticarse ni operar,
     * pero su historial se conserva para trazabilidad.
     * No existe endpoint directo para pasar a este estado; se logra via
     * {@code PATCH /api/v1/usuarios/{id}/estado?estado=INACTIVO}.
     */
    INACTIVO,

    /**
     * Bloqueo temporal. El usuario mantiene sus datos pero no puede acceder.
     * Puede reactivarse mediante {@code PATCH /api/v1/usuarios/{id}/estado?estado=ACTIVO}.
     * Método de dominio relacionado: {@code Usuario#suspender()}.
     */
    SUSPENDIDO,

    /**
     * Bloqueo permanente por razones de seguridad (p. ej. múltiples intentos fallidos).
     * El {@link usuarios.application.service.AuthService} lanza {@code AUTH-002}
     * si se intenta autenticar un usuario en este estado.
     * Método de dominio relacionado: {@code Usuario#bloquear()}.
     */
    BLOQUEADO
}
