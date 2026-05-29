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

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final WebMapper webMapper;

    public UsuarioController(UsuarioUseCase usuarioUseCase, WebMapper webMapper) {
        this.usuarioUseCase = usuarioUseCase;
        this.webMapper      = webMapper;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(webMapper.toResponse(usuarioUseCase.crearUsuario(webMapper.toDomain(request))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.obtenerUsuarioPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(
                usuarioUseCase.listarUsuarios().stream()
                        .map(webMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un usuario")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(
                webMapper.toResponse(usuarioUseCase.actualizarUsuario(id, webMapper.toDomain(request))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioUseCase.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de un usuario (ACTIVO, BLOQUEADO, SUSPENDIDO, INACTIVO)")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id,
                                                          @RequestParam String estado) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.cambiarEstado(id, estado)));
    }

    @PostMapping("/{usuarioId}/grupos/{grupoId}")
    @Operation(summary = "Asignar grupo a usuario")
    public ResponseEntity<UsuarioResponse> asignarGrupo(@PathVariable Long usuarioId,
                                                          @PathVariable Long grupoId) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.asignarGrupo(usuarioId, grupoId)));
    }

    @DeleteMapping("/{usuarioId}/grupos/{grupoId}")
    @Operation(summary = "Remover grupo de usuario")
    public ResponseEntity<UsuarioResponse> removerGrupo(@PathVariable Long usuarioId,
                                                          @PathVariable Long grupoId) {
        return ResponseEntity.ok(webMapper.toResponse(usuarioUseCase.removerGrupo(usuarioId, grupoId)));
    }
}
