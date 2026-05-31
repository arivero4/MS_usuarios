package usuarios.application.port.in;

import usuarios.domain.model.Grupo;

import java.util.List;

/**
 * Puerto de entrada que define los casos de uso de gestión de grupos (roles).
 *
 * <p>Un grupo es un conjunto de privilegios que se asigna a usuarios para
 * controlar su acceso al sistema. Ejemplos: ADMINISTRADOR, PRODUCTOR,
 * PROPIETARIO, ASISTENTE_TECNICO.</p>
 *
 * <p>Implementado por: {@link usuarios.application.service.GrupoService}.</p>
 */
public interface GrupoUseCase {

    /**
     * Crea un nuevo grupo validando que el nombre sea único.
     * Expuesto por {@code POST /api/v1/grupos}.
     *
     * @param grupo Objeto de dominio con nombre y descripción.
     * @return El grupo creado con ID generado y estado ACTIVO.
     * @throws usuarios.domain.exception.UsuarioException si el nombre ya existe (GRP-002).
     */
    Grupo crearGrupo(Grupo grupo);

    /**
     * Obtiene un grupo por su ID con sus privilegios cargados.
     * Expuesto por {@code GET /api/v1/grupos/{id}}.
     *
     * @param id ID del grupo.
     * @return El grupo encontrado.
     * @throws usuarios.domain.exception.GrupoNotFoundException si no existe (GRP-001).
     */
    Grupo obtenerGrupoPorId(Long id);

    /**
     * Lista todos los grupos del sistema con sus privilegios.
     * Expuesto por {@code GET /api/v1/grupos}.
     *
     * @return Lista de grupos. Puede ser vacía, nunca null.
     */
    List<Grupo> listarGrupos();

    /**
     * Actualiza nombre y descripción de un grupo existente.
     * Expuesto por {@code PUT /api/v1/grupos/{id}}.
     *
     * @param id    ID del grupo.
     * @param datos Nuevos valores.
     * @return El grupo actualizado.
     * @throws usuarios.domain.exception.GrupoNotFoundException si no existe.
     */
    Grupo actualizarGrupo(Long id, Grupo datos);

    /**
     * Elimina un grupo del sistema.
     * Falla a nivel de BD si hay usuarios asignados a este grupo
     * (constraint {@code FK_UG_GRUPO} en {@code GRUPOS_USUARIO}).
     * Expuesto por {@code DELETE /api/v1/grupos/{id}}.
     *
     * @param id ID del grupo.
     * @throws usuarios.domain.exception.GrupoNotFoundException si no existe.
     */
    void eliminarGrupo(Long id);

    /**
     * Asigna un privilegio a un grupo (operación idempotente).
     * Expuesto por {@code POST /api/v1/grupos/{grupoId}/privilegios/{privilegioId}}.
     *
     * @param grupoId      ID del grupo receptor.
     * @param privilegioId ID del privilegio a asignar.
     * @return El grupo con la lista de privilegios actualizada.
     * @throws usuarios.domain.exception.GrupoNotFoundException si el grupo no existe.
     * @throws usuarios.domain.exception.UsuarioException       si el privilegio no existe (PRV-001).
     */
    Grupo asignarPrivilegio(Long grupoId, Long privilegioId);

    /**
     * Remueve un privilegio de un grupo (operación idempotente).
     * Expuesto por {@code DELETE /api/v1/grupos/{grupoId}/privilegios/{privilegioId}}.
     *
     * @param grupoId      ID del grupo.
     * @param privilegioId ID del privilegio a remover.
     * @return El grupo con la lista de privilegios actualizada.
     * @throws usuarios.domain.exception.GrupoNotFoundException si el grupo no existe.
     */
    Grupo removerPrivilegio(Long grupoId, Long privilegioId);
}
