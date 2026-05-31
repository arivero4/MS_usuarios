package usuarios.application.port.out;

import usuarios.domain.model.Grupo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida que abstrae la persistencia de grupos (roles).
 *
 * <p>Implementado por:
 * {@link usuarios.infrastructure.adapter.out.persistence.repository.GrupoRepositoryAdapter}
 * usando {@code JdbcTemplate} contra Oracle 10g.</p>
 *
 * <p>Las operaciones de persistencia gestionan implícitamente la tabla
 * {@code PRIVILEGIO_GRUPO} (relación N:M entre grupos y privilegios).</p>
 */
public interface GrupoRepositoryPort {

    /**
     * Persiste un nuevo grupo. La secuencia {@code SEQ_GRUPOS} asigna el ID.
     *
     * @param grupo Objeto de dominio con nombre, descripción y estado.
     * @return El grupo con el ID generado por Oracle.
     */
    Grupo guardar(Grupo grupo);

    /**
     * Busca un grupo por su ID con sus privilegios cargados.
     *
     * @param id ID del grupo.
     * @return {@link Optional} con el grupo si existe, vacío si no.
     */
    Optional<Grupo> buscarPorId(Long id);

    /**
     * Recupera todos los grupos del sistema con sus privilegios.
     *
     * @return Lista de grupos. Vacía si no hay ninguno, nunca null.
     */
    List<Grupo> buscarTodos();

    /**
     * Actualiza nombre y descripción de un grupo. También sincroniza
     * la tabla {@code PRIVILEGIO_GRUPO} con la lista actual de privilegios.
     *
     * @param grupo Objeto de dominio con los datos actualizados (debe tener ID).
     * @return El grupo con los datos actualizados.
     */
    Grupo actualizar(Grupo grupo);

    /**
     * Elimina un grupo de la BD. Primero elimina sus registros en
     * {@code PRIVILEGIO_GRUPO} para respetar las claves foráneas.
     * Fallará si hay usuarios asignados (constraint {@code FK_UG_GRUPO}).
     *
     * @param id ID del grupo a eliminar.
     */
    void eliminar(Long id);

    /**
     * Verifica si ya existe un grupo con ese nombre (unicidad en {@code UK_GRUPOS_NOMBRE}).
     *
     * @param nombre Nombre del grupo a verificar.
     * @return {@code true} si el nombre ya está registrado.
     */
    boolean existePorNombre(String nombre);
}
