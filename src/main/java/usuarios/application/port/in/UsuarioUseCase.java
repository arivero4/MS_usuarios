package usuarios.application.port.in;

import usuarios.domain.model.Usuario;

import java.util.List;

public interface UsuarioUseCase {

    Usuario crearUsuario(Usuario usuario);

    Usuario obtenerUsuarioPorId(Long id);

    Usuario obtenerUsuarioPorCorreo(String correo);

    List<Usuario> listarUsuarios();

    Usuario actualizarUsuario(Long id, Usuario usuario);

    void eliminarUsuario(Long id);

    Usuario cambiarEstado(Long id, String nuevoEstado);

    Usuario asignarGrupo(Long usuarioId, Long grupoId);

    Usuario removerGrupo(Long usuarioId, Long grupoId);
}
