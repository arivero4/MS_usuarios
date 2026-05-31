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

/**
 * Servicio de aplicación que implementa los casos de uso de gestión de usuarios.
 *
 * <p>Orquesta la lógica de negocio combinando los puertos de salida
 * (repositorio, codificador de contraseñas, grupos) sin acoplar el dominio
 * a ninguna tecnología concreta.</p>
 *
 * <p>Implementa el puerto de entrada {@link UsuarioUseCase}, que es la única
 * interfaz que los adaptadores de entrada (controladores REST) deben conocer.</p>
 *
 * <p>Dependencias inyectadas por constructor (principio de inversión de dependencias):</p>
 * <ul>
 *   <li>{@link UsuarioRepositoryPort} – abstracción de la BD (implementado por JDBC).</li>
 *   <li>{@link GrupoRepositoryPort}   – para validar que el grupo existe al asignarlo.</li>
 *   <li>{@link PasswordEncoderPort}   – abstracción de BCrypt.</li>
 * </ul>
 */
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

    /**
     * Crea un nuevo usuario aplicando las siguientes reglas de negocio:
     * <ol>
     *   <li>Verifica que el correo no esté ya registrado; si existe lanza
     *       {@link UsuarioAlreadyExistsException} (HTTP 409).</li>
     *   <li>Cifra la contraseña en texto plano con BCrypt antes de persistir.</li>
     *   <li>Fija el estado inicial en {@link Estado#ACTIVO}.</li>
     *   <li>Asigna las fechas de creación y actualización al instante actual.</li>
     * </ol>
     *
     * @param usuario Objeto de dominio con los datos del nuevo usuario (contraseña en texto plano).
     * @return El usuario creado con ID generado por Oracle y contraseña cifrada.
     * @throws UsuarioAlreadyExistsException si el correo ya existe (código {@code USR-002}).
     */
    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existePorCorreo(usuario.getCorreo())) {
            throw new UsuarioAlreadyExistsException(usuario.getCorreo());
        }
        // El puerto de salida oculta la implementación BCrypt:
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setEstado(Estado.ACTIVO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.guardar(usuario);
    }

    /**
     * Recupera un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return El usuario encontrado con sus grupos y privilegios cargados.
     * @throws UsuarioNotFoundException si no existe ningún usuario con ese ID (código {@code USR-001}).
     */
    @Override
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    /**
     * Recupera un usuario por su correo electrónico.
     * Usado principalmente por {@link AuthService#autenticar} durante el login.
     *
     * @param correo Correo del usuario.
     * @return El usuario encontrado.
     * @throws UsuarioNotFoundException si el correo no está registrado (código {@code USR-001}).
     */
    @Override
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(() -> new UsuarioNotFoundException(correo));
    }

    /**
     * Lista todos los usuarios del sistema sin paginación.
     *
     * @return Lista con todos los usuarios y sus grupos cargados. Puede ser vacía.
     */
    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.buscarTodos();
    }

    /**
     * Actualiza los datos editables de un usuario (nombre, teléfono, tarjeta profesional).
     * Nota: correo y contraseña no se actualizan aquí para mayor seguridad;
     * requieren flujos separados.
     *
     * @param id    ID del usuario a actualizar.
     * @param datos Objeto con los nuevos valores.
     * @return El usuario actualizado.
     * @throws UsuarioNotFoundException si el ID no existe (código {@code USR-001}).
     */
    @Override
    public Usuario actualizarUsuario(Long id, Usuario datos) {
        // Carga el usuario existente para no sobreescribir campos que no vienen en la petición:
        Usuario existente = obtenerUsuarioPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setTelefono(datos.getTelefono());
        existente.setNumeroTarjetaProfesional(datos.getNumeroTarjetaProfesional());
        existente.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.actualizar(existente);
    }

    /**
     * Elimina definitivamente un usuario del sistema.
     * Se valida primero que exista para devolver 404 en lugar de operar silenciosamente.
     *
     * @param id ID del usuario a eliminar.
     * @throws UsuarioNotFoundException si el ID no existe (código {@code USR-001}).
     */
    @Override
    public void eliminarUsuario(Long id) {
        obtenerUsuarioPorId(id); // Lanza 404 si no existe
        usuarioRepository.eliminar(id);
    }

    /**
     * Cambia el estado del usuario validando que el nuevo estado sea uno de los
     * valores del enum {@link Estado}.
     *
     * <p>Usa el switch expression de Java 14+ con pattern matching sobre el enum.
     * Si el estado es inválido (no reconocido), lanza {@code USR-003}.</p>
     *
     * @param id          ID del usuario.
     * @param nuevoEstado String del nuevo estado (case-insensitive: "activo", "ACTIVO", etc.).
     * @return El usuario con el nuevo estado aplicado.
     * @throws UsuarioNotFoundException si el usuario no existe (código {@code USR-001}).
     * @throws UsuarioException         si el estado es inválido (código {@code USR-003}).
     */
    @Override
    public Usuario cambiarEstado(Long id, String nuevoEstado) {
        Usuario usuario = obtenerUsuarioPorId(id);
        try {
            switch (Estado.valueOf(nuevoEstado.toUpperCase())) {
                case ACTIVO     -> usuario.activar();     // Delega en métodos de dominio
                case BLOQUEADO  -> usuario.bloquear();
                case SUSPENDIDO -> usuario.suspender();
                default -> throw new UsuarioException("USR-003", "Estado no permitido: " + nuevoEstado);
            }
        } catch (IllegalArgumentException e) {
            // Estado.valueOf lanza IAE si el string no corresponde a ningún valor del enum:
            throw new UsuarioException("USR-003", "Estado inválido: " + nuevoEstado);
        }
        return usuarioRepository.actualizar(usuario);
    }

    /**
     * Asigna un grupo (rol) al usuario si aún no lo tiene.
     * La comprobación de idempotencia evita duplicados en la tabla {@code GRUPOS_USUARIO}.
     *
     * @param usuarioId ID del usuario.
     * @param grupoId   ID del grupo a asignar.
     * @return El usuario con la lista de grupos actualizada.
     * @throws UsuarioNotFoundException si el usuario no existe (código {@code USR-001}).
     * @throws GrupoNotFoundException   si el grupo no existe (código {@code GRP-001}).
     */
    @Override
    public Usuario asignarGrupo(Long usuarioId, Long grupoId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        Grupo grupo = grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new GrupoNotFoundException(grupoId));
        // Comprobación de idempotencia: solo añade si el grupo no estaba ya asignado:
        boolean yaAsignado = usuario.getGrupos().stream()
                .anyMatch(g -> g.getId().equals(grupoId));
        if (!yaAsignado) {
            usuario.getGrupos().add(grupo);
            usuario.setFechaActualizacion(LocalDateTime.now());
            return usuarioRepository.actualizar(usuario);
        }
        return usuario; // Si ya estaba asignado devuelve sin modificar
    }

    /**
     * Remueve un grupo del usuario. Si el usuario no tenía ese grupo,
     * la operación es silenciosa (idempotente).
     *
     * @param usuarioId ID del usuario.
     * @param grupoId   ID del grupo a remover.
     * @return El usuario con la lista de grupos actualizada.
     * @throws UsuarioNotFoundException si el usuario no existe (código {@code USR-001}).
     */
    @Override
    public Usuario removerGrupo(Long usuarioId, Long grupoId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        // removeIf elimina el elemento que cumpla la condición sin lanzar excepción si no existe:
        usuario.getGrupos().removeIf(g -> g.getId().equals(grupoId));
        usuario.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.actualizar(usuario);
    }
}
