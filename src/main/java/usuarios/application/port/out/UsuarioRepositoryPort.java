package usuarios.application.port.out;

import usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorCorreo(String correo);

    Optional<Usuario> buscarPorNumeroIdentificacion(String numeroIdentificacion);

    List<Usuario> buscarTodos();

    Usuario actualizar(Usuario usuario);

    void eliminar(Long id);

    boolean existePorCorreo(String correo);

    boolean existePorNumeroIdentificacion(String numeroIdentificacion);
}
