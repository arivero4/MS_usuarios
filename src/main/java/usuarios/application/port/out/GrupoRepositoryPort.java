package usuarios.application.port.out;

import usuarios.domain.model.Grupo;

import java.util.List;
import java.util.Optional;

public interface GrupoRepositoryPort {

    Grupo guardar(Grupo grupo);

    Optional<Grupo> buscarPorId(Long id);

    List<Grupo> buscarTodos();

    Grupo actualizar(Grupo grupo);

    void eliminar(Long id);

    boolean existePorNombre(String nombre);
}
