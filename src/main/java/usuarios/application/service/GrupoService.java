package usuarios.application.service;

import org.springframework.stereotype.Service;
import usuarios.application.port.in.GrupoUseCase;
import usuarios.application.port.out.GrupoRepositoryPort;
import usuarios.application.port.out.PrivilegioRepositoryPort;
import usuarios.domain.exception.GrupoNotFoundException;
import usuarios.domain.exception.UsuarioException;
import usuarios.domain.enums.Estado;
import usuarios.domain.model.Grupo;
import usuarios.domain.model.Privilegio;

import java.util.List;

/**
 * Servicio de aplicación que implementa los casos de uso de gestión de grupos (roles).
 *
 * <p>Un grupo agrupa usuarios con el mismo perfil de acceso (ADMINISTRADOR,
 * PROPIETARIO, PRODUCTOR, ASISTENTE_TECNICO). Los grupos contienen
 * {@link Privilegio}s que definen qué acciones pueden realizar sus miembros.</p>
 */
@Service
public class GrupoService implements GrupoUseCase {

    private final GrupoRepositoryPort grupoRepository;
    /** Puerto de salida para validar que el privilegio existe antes de asignarlo. */
    private final PrivilegioRepositoryPort privilegioRepository;

    public GrupoService(GrupoRepositoryPort grupoRepository,
                        PrivilegioRepositoryPort privilegioRepository) {
        this.grupoRepository      = grupoRepository;
        this.privilegioRepository = privilegioRepository;
    }

    /**
     * Crea un nuevo grupo validando que el nombre no esté ya registrado.
     * Fija el estado inicial en {@link Estado#ACTIVO}.
     *
     * @param grupo Objeto de dominio con los datos del nuevo grupo.
     * @return El grupo creado con ID generado por Oracle.
     * @throws UsuarioException si el nombre del grupo ya existe (código {@code GRP-002}).
     */
    @Override
    public Grupo crearGrupo(Grupo grupo) {
        if (grupoRepository.existePorNombre(grupo.getNombre())) {
            throw new UsuarioException("GRP-002", "Ya existe un grupo con el nombre: " + grupo.getNombre());
        }
        grupo.setEstado(Estado.ACTIVO);
        return grupoRepository.guardar(grupo);
    }

    /**
     * Recupera un grupo por su ID con sus privilegios cargados.
     *
     * @param id ID del grupo.
     * @return El grupo encontrado.
     * @throws GrupoNotFoundException si no existe (código {@code GRP-001}).
     */
    @Override
    public Grupo obtenerGrupoPorId(Long id) {
        return grupoRepository.buscarPorId(id)
                .orElseThrow(() -> new GrupoNotFoundException(id));
    }

    /**
     * Lista todos los grupos del sistema con sus privilegios.
     *
     * @return Lista de grupos. Puede ser vacía.
     */
    @Override
    public List<Grupo> listarGrupos() {
        return grupoRepository.buscarTodos();
    }

    /**
     * Actualiza nombre y descripción de un grupo existente.
     *
     * @param id    ID del grupo a actualizar.
     * @param datos Objeto con los nuevos valores.
     * @return El grupo actualizado.
     * @throws GrupoNotFoundException si el grupo no existe (código {@code GRP-001}).
     */
    @Override
    public Grupo actualizarGrupo(Long id, Grupo datos) {
        Grupo existente = obtenerGrupoPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        return grupoRepository.actualizar(existente);
    }

    /**
     * Elimina un grupo del sistema.
     * No verifica si hay usuarios asignados; la BD lanzará constraint violation
     * si existen registros en {@code GRUPOS_USUARIO} que referencian este grupo.
     *
     * @param id ID del grupo a eliminar.
     * @throws GrupoNotFoundException si el grupo no existe (código {@code GRP-001}).
     */
    @Override
    public void eliminarGrupo(Long id) {
        obtenerGrupoPorId(id); // Valida existencia antes de eliminar
        grupoRepository.eliminar(id);
    }

    /**
     * Asigna un privilegio al grupo si aún no lo tiene (operación idempotente).
     *
     * @param grupoId      ID del grupo destinatario.
     * @param privilegioId ID del privilegio a asignar.
     * @return El grupo con la lista de privilegios actualizada.
     * @throws GrupoNotFoundException si el grupo no existe (código {@code GRP-001}).
     * @throws UsuarioException       si el privilegio no existe (código {@code PRV-001}).
     */
    @Override
    public Grupo asignarPrivilegio(Long grupoId, Long privilegioId) {
        Grupo grupo = obtenerGrupoPorId(grupoId);
        Privilegio privilegio = privilegioRepository.buscarPorId(privilegioId)
                .orElseThrow(() -> new UsuarioException("PRV-001", "Privilegio no encontrado: " + privilegioId));
        // Comprobación de idempotencia: solo añade si no estaba ya asignado:
        boolean yaAsignado = grupo.getPrivilegios().stream()
                .anyMatch(p -> p.getId().equals(privilegioId));
        if (!yaAsignado) {
            grupo.getPrivilegios().add(privilegio);
            return grupoRepository.actualizar(grupo);
        }
        return grupo;
    }

    /**
     * Remueve un privilegio del grupo. Operación silenciosa si el privilegio
     * no estaba asignado.
     *
     * @param grupoId      ID del grupo.
     * @param privilegioId ID del privilegio a remover.
     * @return El grupo con la lista de privilegios actualizada.
     * @throws GrupoNotFoundException si el grupo no existe (código {@code GRP-001}).
     */
    @Override
    public Grupo removerPrivilegio(Long grupoId, Long privilegioId) {
        Grupo grupo = obtenerGrupoPorId(grupoId);
        grupo.getPrivilegios().removeIf(p -> p.getId().equals(privilegioId));
        return grupoRepository.actualizar(grupo);
    }
}
