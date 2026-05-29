package usuarios.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usuarios.application.port.in.GrupoUseCase;
import usuarios.infrastructure.adapter.in.web.dto.GrupoRequest;
import usuarios.infrastructure.adapter.in.web.dto.GrupoResponse;
import usuarios.infrastructure.adapter.in.web.mapper.WebMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/grupos")
@Tag(name = "Grupos", description = "Gestión de grupos de usuarios")
public class GrupoController {

    private final GrupoUseCase grupoUseCase;
    private final WebMapper webMapper;

    public GrupoController(GrupoUseCase grupoUseCase, WebMapper webMapper) {
        this.grupoUseCase = grupoUseCase;
        this.webMapper    = webMapper;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo grupo")
    public ResponseEntity<GrupoResponse> crear(@Valid @RequestBody GrupoRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(webMapper.toResponse(grupoUseCase.crearGrupo(webMapper.toDomain(request))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener grupo por ID")
    public ResponseEntity<GrupoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(webMapper.toResponse(grupoUseCase.obtenerGrupoPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar todos los grupos")
    public ResponseEntity<List<GrupoResponse>> listar() {
        return ResponseEntity.ok(
                grupoUseCase.listarGrupos().stream()
                        .map(webMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un grupo")
    public ResponseEntity<GrupoResponse> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody GrupoRequest request) {
        return ResponseEntity.ok(
                webMapper.toResponse(grupoUseCase.actualizarGrupo(id, webMapper.toDomain(request))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un grupo")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        grupoUseCase.eliminarGrupo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{grupoId}/privilegios/{privilegioId}")
    @Operation(summary = "Asignar privilegio a grupo")
    public ResponseEntity<GrupoResponse> asignarPrivilegio(@PathVariable Long grupoId,
                                                             @PathVariable Long privilegioId) {
        return ResponseEntity.ok(webMapper.toResponse(grupoUseCase.asignarPrivilegio(grupoId, privilegioId)));
    }

    @DeleteMapping("/{grupoId}/privilegios/{privilegioId}")
    @Operation(summary = "Remover privilegio de grupo")
    public ResponseEntity<GrupoResponse> removerPrivilegio(@PathVariable Long grupoId,
                                                             @PathVariable Long privilegioId) {
        return ResponseEntity.ok(webMapper.toResponse(grupoUseCase.removerPrivilegio(grupoId, privilegioId)));
    }
}
