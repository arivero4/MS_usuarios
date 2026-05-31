package usuarios.domain.exception;

/**
 * Se lanza cuando no existe un grupo con el ID solicitado.
 *
 * <p>El {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}
 * la captura y responde con {@code HTTP 404 Not Found}.</p>
 *
 * <p>Código de error del dominio: {@code GRP-001}.</p>
 *
 * <p>Casos de uso donde se lanza:</p>
 * <ul>
 *   <li>{@link usuarios.application.service.GrupoService#obtenerGrupoPorId}.</li>
 *   <li>{@link usuarios.application.service.UsuarioService#asignarGrupo} –
 *       cuando el grupo destino no existe.</li>
 * </ul>
 */
public class GrupoNotFoundException extends UsuarioException {

    /**
     * @param id ID del grupo no encontrado.
     */
    public GrupoNotFoundException(Long id) {
        super("GRP-001", "Grupo no encontrado con id: " + id);
    }
}
