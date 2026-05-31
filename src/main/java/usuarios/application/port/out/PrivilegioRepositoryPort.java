package usuarios.application.port.out;

import usuarios.domain.model.Privilegio;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida que abstrae la persistencia de privilegios.
 *
 * <p>Los privilegios son datos de catálogo: se crean en el script de migración
 * {@code V2__create_grupo.sql} y raramente se modifican en tiempo de ejecución.
 * Por eso solo se exponen operaciones de lectura y verificación.</p>
 *
 * <p>Implementado por:
 * {@link usuarios.infrastructure.adapter.out.persistence.repository.PrivilegioRepositoryAdapter}
 * usando {@code JdbcTemplate} contra Oracle 10g.</p>
 */
public interface PrivilegioRepositoryPort {

    /**
     * Busca un privilegio por su ID.
     * Usado por {@link usuarios.application.service.GrupoService#asignarPrivilegio}
     * para validar que el privilegio existe antes de asignarlo a un grupo.
     *
     * @param id ID del privilegio.
     * @return {@link Optional} con el privilegio si existe, vacío si no.
     */
    Optional<Privilegio> buscarPorId(Long id);

    /**
     * Recupera todos los privilegios del catálogo.
     *
     * @return Lista de privilegios. Vacía si no hay ninguno, nunca null.
     */
    List<Privilegio> buscarTodos();

    /**
     * Verifica si ya existe un privilegio con ese código único.
     *
     * @param codigo Código del privilegio (ej: "USR_CREAR").
     * @return {@code true} si el código ya está registrado.
     */
    boolean existePorCodigo(String codigo);
}
