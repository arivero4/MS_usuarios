package usuarios.application.service;

import org.springframework.stereotype.Service;
import usuarios.application.port.in.UsuarioUseCase;
import usuarios.application.port.out.GrupoRepositoryPort;
import usuarios.application.port.out.PasswordEncoderPort;
import usuarios.application.port.out.UsuarioRepositoryPort;
import usuarios.domain.enums.Estado;
import usuarios.domain.exception.GrupoNotFoundException;
import usuarios.domain.exception.UsuarioAlreadyExistsException;
import usuarios.domain.exception.UsuarioException;
import usuarios.domain.exception.UsuarioNotFoundException;
import usuarios.domain.model.Grupo;
import usuarios.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService implements UsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final GrupoRepositoryPort grupoRepository;
    private final PasswordEncoderPort passwordEncoder;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository,
                          GrupoRepositoryPort grupoRepository,
                          PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository   = grupoRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existePorCorreo(usuario.getCorreo())) {
            throw new UsuarioAlreadyExistsException(usuario.getCorreo());
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setEstado(Estado.ACTIVO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.guardar(usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    @Override
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(() -> new UsuarioNotFoundException(correo));
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.buscarTodos();
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario datos) {
        Usuario existente = obtenerUsuarioPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setTelefono(datos.getTelefono());
        existente.setNumeroTarjetaProfesional(datos.getNumeroTarjetaProfesional());
        existente.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.actualizar(existente);
    }

    @Override
    public void eliminarUsuario(Long id) {
        obtenerUsuarioPorId(id);
        usuarioRepository.eliminar(id);
    }

    @Override
    public Usuario cambiarEstado(Long id, String nuevoEstado) {
        Usuario usuario = obtenerUsuarioPorId(id);
        try {
            switch (Estado.valueOf(nuevoEstado.toUpperCase())) {
                case ACTIVO     -> usuario.activar();
                case BLOQUEADO  -> usuario.bloquear();
                case SUSPENDIDO -> usuario.suspender();
                default -> throw new UsuarioException("USR-003", "Estado no permitido: " + nuevoEstado);
            }
        } catch (IllegalArgumentException e) {
            throw new UsuarioException("USR-003", "Estado inválido: " + nuevoEstado);
        }
        return usuarioRepository.actualizar(usuario);
    }

    @Override
    public Usuario asignarGrupo(Long usuarioId, Long grupoId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        Grupo grupo = grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new GrupoNotFoundException(grupoId));
        boolean yaAsignado = usuario.getGrupos().stream()
                .anyMatch(g -> g.getId().equals(grupoId));
        if (!yaAsignado) {
            usuario.getGrupos().add(grupo);
            usuario.setFechaActualizacion(LocalDateTime.now());
            return usuarioRepository.actualizar(usuario);
        }
        return usuario;
    }

    @Override
    public Usuario removerGrupo(Long usuarioId, Long grupoId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        usuario.getGrupos().removeIf(g -> g.getId().equals(grupoId));
        usuario.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.actualizar(usuario);
    }
}
