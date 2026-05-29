package usuarios.application.port.out;

import usuarios.domain.model.Privilegio;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de Privilegio.
 * La aplicación lo define; el adaptador JDBC lo implementa.
 */
public interface PrivilegioRepositoryPort {

    Optional<Privilegio> buscarPorId(Long id);

    List<Privilegio> buscarTodos();

    boolean existePorCodigo(String codigo);
}
