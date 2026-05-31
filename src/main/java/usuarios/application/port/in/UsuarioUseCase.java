package usuarios.application.port.in;

import usuarios.domain.model.Usuario;

import java.util.List;

/**
 * Puerto de entrada (driving port) que define los casos de uso de gestión de usuarios.
 *
 * <p>En la arquitectura hexagonal, los puertos de entrada representan las operaciones
 * que el núcleo de la aplicación ofrece al mundo exterior. Los adaptadores de entrada
 * (controladores REST) solo conocen esta interfaz, nunca la implementación concreta
 * ({@link usuarios.application.service.UsuarioService}).</p>
 *
 * <p>Principio de Inversión de Dependencias (DIP): los controladores dependen de esta
 * abstracción, no de la implementación. Esto permite cambiar la lógica sin modificar
 * los controladores.</p>
 *
 * <p>Implementado por: {@link usuarios.application.service.UsuarioService}.</p>
 */
public interface UsuarioUseCase {

    /**
     * Crea un nuevo usuario en el sistema aplicando todas las reglas de negocio:
     * unicidad de correo, cifrado de contraseña, estado inicial ACTIVO.
     *
     * @param usuario Objeto de dominio con los datos del nuevo usuario (contraseña en texto plano).
     * @return El usuario persistido con ID generado y contraseña cifrada.
     * @throws usuarios.domain.exception.UsuarioAlreadyExistsException si el correo ya existe.
     */
    Usuario crearUsuario(Usuario usuario);

    /**
     * Obtiene un usuario por su ID con sus grupos y privilegios cargados.
     *
     * @param id Identificador único del usuario.
     * @return El usuario encontrado.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si no existe.
     */
    Usuario obtenerUsuarioPorId(Long id);

    /**
     * Obtiene un usuario por su correo electrónico.
     * Usado principalmente en el flujo de autenticación.
     *
     * @param correo Correo electrónico único del usuario.
     * @return El usuario encontrado.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si no existe.
     */
    Usuario obtenerUsuarioPorCorreo(String correo);

    /**
     * Lista todos los usuarios del sistema con sus grupos y privilegios.
     *
     * @return Lista de usuarios. Puede ser vacía, nunca null.
     */
    List<Usuario> listarUsuarios();

    /**
     * Actualiza los campos editables del usuario (nombre, teléfono, tarjeta profesional).
     * Correo y contraseña tienen flujos separados por seguridad.
     *
     * @param id    ID del usuario a actualizar.
     * @param datos Objeto con los nuevos valores.
     * @return El usuario con los datos actualizados.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si no existe.
     */
    Usuario actualizarUsuario(Long id, Usuario datos);

    /**
     * Elimina definitivamente un usuario del sistema.
     *
     * @param id ID del usuario a eliminar.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si no existe.
     */
    void eliminarUsuario(Long id);

    /**
     * Cambia el estado del usuario (ACTIVO, INACTIVO, SUSPENDIDO, BLOQUEADO).
     * Expuesto por {@code PATCH /api/v1/usuarios/{id}/estado?estado=ACTIVO}.
     *
     * @param id          ID del usuario.
     * @param nuevoEstado Nombre del nuevo estado (case-insensitive).
     * @return El usuario con el estado actualizado.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si no existe.
     * @throws usuarios.domain.exception.UsuarioException         si el estado es inválido.
     */
    Usuario cambiarEstado(Long id, String nuevoEstado);

    /**
     * Asigna un grupo (rol) a un usuario (operación idempotente).
     * Expuesto por {@code POST /api/v1/usuarios/{usuarioId}/grupos/{grupoId}}.
     *
     * @param usuarioId ID del usuario receptor.
     * @param grupoId   ID del grupo a asignar.
     * @return El usuario con la lista de grupos actualizada.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si el usuario no existe.
     * @throws usuarios.domain.exception.GrupoNotFoundException   si el grupo no existe.
     */
    Usuario asignarGrupo(Long usuarioId, Long grupoId);

    /**
     * Remueve un grupo de un usuario (operación idempotente).
     * Expuesto por {@code DELETE /api/v1/usuarios/{usuarioId}/grupos/{grupoId}}.
     *
     * @param usuarioId ID del usuario.
     * @param grupoId   ID del grupo a remover.
     * @return El usuario con la lista de grupos actualizada.
     * @throws usuarios.domain.exception.UsuarioNotFoundException si el usuario no existe.
     */
    Usuario removerGrupo(Long usuarioId, Long grupoId);
}
