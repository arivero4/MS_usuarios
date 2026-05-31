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

/**
 * Controlador REST para la gestión de grupos (roles) del sistema.
 *
 * <p>Base URL: {@code /api/v1/grupos}</p>
 *
 * <p>Endpoints:</p>
 * <pre>
 *  POST   /api/v1/grupos                              → Crear grupo            [AUTENTICADO]
 *  GET    /api/v1/grupos                              → Listar grupos          [AUTENTICADO]
 *  GET    /api/v1/grupos/{id}                         → Obtener por ID         [AUTENTICADO]
 *  PUT    /api/v1/grupos/{id}                         → Actualizar grupo       [AUTENTICADO]
 *  DELETE /api/v1/grupos/{id}                         → Eliminar grupo         [AUTENTICADO]
 *  POST   /api/v1/grupos/{gid}/privilegios/{pid}      → Asignar privilegio     [AUTENTICADO]
 *  DELETE /api/v1/grupos/{gid}/privilegios/{pid}      → Remover privilegio     [AUTENTICADO]
 * </pre>
 */
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

    /**
     * Crea un nuevo grupo (rol) en el sistema.
     * El nombre debe ser único (validado por el servicio).
     *
     * @param request DTO con nombre (requerido) y descripción (opcional).
     * @return {@code 201 Created} con el {@link GrupoResponse} del grupo creado.
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo grupo")
    public ResponseEntity<GrupoResponse> crear(@Valid @RequestBody GrupoRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(webMapper.toResponse(grupoUseCase.crearGrupo(webMapper.toDomain(request))));
    }

    /**
     * Obtiene un grupo por su ID con sus privilegios.
     *
     * @param id ID del grupo.
     * @return {@code 200 OK} con el {@link GrupoResponse}, o {@code 404} si no existe.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener grupo por ID")
    public ResponseEntity<GrupoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(webMapper.toResponse(grupoUseCase.obtenerGrupoPorId(id)));
    }

    /**
     * Lista todos los grupos del sistema con sus privilegios.
     *
     * @return {@code 200 OK} con lista de {@link GrupoResponse}.
     */
    @GetMapping
    @Operation(summary = "Listar todos los grupos")
    public ResponseEntity<List<GrupoResponse>> listar() {
        return ResponseEntity.ok(
                grupoUseCase.listarGrupos().stream()
                        .map(webMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    /**
     * Actualiza nombre y descripción de un grupo.
     *
     * @param id      ID del grupo.
     * @param request DTO con los nuevos valores.
     * @return {@code 200 OK} con el grupo actualizado.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un grupo")
    public ResponseEntity<GrupoResponse> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody GrupoRequest request) {
        return ResponseEntity.ok(
                webMapper.toResponse(grupoUseCase.actualizarGrupo(id, webMapper.toDomain(request))));
    }

    /**
     * Elimina un grupo del sistema.
     * Fallará si hay usuarios asignados a este grupo.
     *
     * @param id ID del grupo a eliminar.
     * @return {@code 204 No Content}.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un grupo")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        grupoUseCase.eliminarGrupo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Asigna un privilegio a un grupo. Operación idempotente.
     *
     * @param grupoId      ID del grupo receptor.
     * @param privilegioId ID del privilegio a asignar.
     * @return {@code 200 OK} con el grupo y su lista de privilegios actualizada.
     */
    @PostMapping("/{grupoId}/privilegios/{privilegioId}")
    @Operation(summary = "Asignar privilegio a grupo")
    public ResponseEntity<GrupoResponse> asignarPrivilegio(@PathVariable Long grupoId,
                                                             @PathVariable Long privilegioId) {
        return ResponseEntity.ok(webMapper.toResponse(grupoUseCase.asignarPrivilegio(grupoId, privilegioId)));
    }

    /**
     * Remueve un privilegio de un grupo. Operación idempotente.
     *
     * @param grupoId      ID del grupo.
     * @param privilegioId ID del privilegio a remover.
     * @return {@code 200 OK} con el grupo y su lista de privilegios actualizada.
     */
    @DeleteMapping("/{grupoId}/privilegios/{privilegioId}")
    @Operation(summary = "Remover privilegio de grupo")
    public ResponseEntity<GrupoResponse> removerPrivilegio(@PathVariable Long grupoId,
                                                             @PathVariable Long privilegioId) {
        return ResponseEntity.ok(webMapper.toResponse(grupoUseCase.removerPrivilegio(grupoId, privilegioId)));
    }
}
