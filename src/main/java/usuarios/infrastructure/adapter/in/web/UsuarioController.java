package usuarios.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usuarios.application.port.in.UsuarioUseCase;
import usuarios.infrastructure.adapter.in.web.dto.UsuarioRequest;
import usuarios.infrastructure.adapter.in.web.dto.UsuarioResponse;
import usuarios.infrastructure.adapter.in.web.mapper.WebMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para los endpoints de gestión de usuarios.
 *
 * <p>Adaptador de entrada que traduce peticiones HTTP al puerto
 * {@link UsuarioUseCase}. Todos los endpoints requieren autenticación JWT
 * (configurado en {@link usuarios.infrastructure.config.SecurityConfig}).</p>
 *
 * <p>Base URL: {@code /api/v1/usuarios}</p>
 *
 * <p>Endpoints disponibles:</p>
 * <pre>
 *  POST   /api/v1/usuarios                          → Crear usuario           [AUTENTICADO]
 *  GET    /api/v1/usuarios                          → Listar usuarios         [AUTENTICADO]
 *  GET    /api/v1/usuarios/{id}                     → Obtener por ID          [AUTENTICADO]
 *  PUT    /api/v1/usuarios/{id}                     → Actualizar usuario      [AUTENTICADO]
 *  DELETE /api/v1/usuarios/{id}                     → Eliminar usuario        [AUTENTICADO]
 *  PATCH  /api/v1/usuarios/{id}/estado?estado=ACTIVO→ Cambiar estado          [AUTENTICADO]
 *  POST   /api/v1/usuarios/{uid}/grupos/{gid}        → Asignar grupo          [AUTENTICADO]
 *  DELETE /api/v1/usuarios/{uid}/grupos/{gid}        → Remover grupo          [AUTENTICADO]
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    /** Traduce entre DTOs REST y modelos de dominio. */
    private final WebMapper webMapper;

    public UsuarioController(UsuarioUseCase usuarioUseCase, WebMapper webMapper) {
        this.usuarioUseCase = usuarioUseCase;
        this.webMapper      = webMapper;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * <p>Responde con {@code 201 Created} y el usuario creado en el body.</p>
     *
     * @param request DTO validado: numeroIdentificacion, nombre, correo y contraseña son requeridos.
     * @return {@link UsuarioResponse} con todos los datos del usuario creado (sin contraseña).
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo usuario")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created (no 200 OK)
                .body(webMapper.toResponse(usuarioUseCase.crearUsuario(webMapper.toDomain(request))));
    }

    /**
     * Obtiene los datos de un usuario específico por su ID.
     *
     * @param id ID numérico del usuario ({@code @PathVariable}).
     * @return {@code 200 OK} con el {@link UsuarioResponse}, o {@code 404} si no existe.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.obtenerUsuarioPorId(id)));
    }

    /**
     * Lista todos los usuarios del sistema.
     *
     * @return {@code 200 OK} con lista de {@link UsuarioResponse}. Lista vacía si no hay usuarios.
     */
    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(
                usuarioUseCase.listarUsuarios().stream()
                        .map(webMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    /**
     * Actualiza los datos editables de un usuario (nombre, teléfono, tarjeta profesional).
     * El correo y la contraseña no se actualizan por este endpoint.
     *
     * @param id      ID del usuario a actualizar.
     * @param request DTO con los nuevos valores.
     * @return {@code 200 OK} con el usuario actualizado.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un usuario")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(
                webMapper.toResponse(usuarioUseCase.actualizarUsuario(id, webMapper.toDomain(request))));
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param id ID del usuario a eliminar.
     * @return {@code 204 No Content} si fue eliminado, {@code 404} si no existía.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioUseCase.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Cambia el estado de un usuario.
     *
     * <p>Ejemplo de uso: {@code PATCH /api/v1/usuarios/1/estado?estado=BLOQUEADO}</p>
     *
     * @param id     ID del usuario.
     * @param estado Nuevo estado como query parameter: ACTIVO, INACTIVO, SUSPENDIDO, BLOQUEADO.
     * @return {@code 200 OK} con el usuario y su nuevo estado.
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de un usuario (ACTIVO, BLOQUEADO, SUSPENDIDO, INACTIVO)")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id,
                                                          @RequestParam String estado) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.cambiarEstado(id, estado)));
    }

    /**
     * Asigna un grupo (rol) a un usuario. Operación idempotente.
     *
     * @param usuarioId ID del usuario receptor.
     * @param grupoId   ID del grupo a asignar.
     * @return {@code 200 OK} con el usuario y su lista de grupos actualizada.
     */
    @PostMapping("/{usuarioId}/grupos/{grupoId}")
    @Operation(summary = "Asignar grupo a usuario")
    public ResponseEntity<UsuarioResponse> asignarGrupo(@PathVariable Long usuarioId,
                                                          @PathVariable Long grupoId) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.asignarGrupo(usuarioId, grupoId)));
    }

    /**
     * Remueve un grupo de un usuario. Operación idempotente.
     *
     * @param usuarioId ID del usuario.
     * @param grupoId   ID del grupo a remover.
     * @return {@code 200 OK} con el usuario y su lista de grupos actualizada.
     */
    @DeleteMapping("/{usuarioId}/grupos/{grupoId}")
    @Operation(summary = "Remover grupo de usuario")
    public ResponseEntity<UsuarioResponse> removerGrupo(@PathVariable Long usuarioId,
                                                          @PathVariable Long grupoId) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.removerGrupo(usuarioId, grupoId)));
    }
}
