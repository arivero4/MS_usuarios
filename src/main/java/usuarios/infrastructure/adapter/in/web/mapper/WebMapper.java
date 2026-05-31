package usuarios.infrastructure.adapter.in.web.mapper;

import org.springframework.stereotype.Component;
import usuarios.domain.model.Grupo;
import usuarios.domain.model.Privilegio;
import usuarios.domain.model.Usuario;
import usuarios.infrastructure.adapter.in.web.dto.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de la capa web (adaptador de entrada).
 *
 * <p>Convierte entre los DTOs REST (Request/Response) y los modelos de dominio.
 * Es un componente de infraestructura pura: no contiene lógica de negocio
 * y no conoce las entidades de base de datos.</p>
 *
 * <p>Separación de responsabilidades:</p>
 * <ul>
 *   <li>Los DTOs pertenecen a la capa de infraestructura (web).</li>
 *   <li>Los modelos de dominio pertenecen al núcleo hexagonal.</li>
 *   <li>Este mapper traduce entre ambas capas en los dos sentidos.</li>
 * </ul>
 */
@Component
public class WebMapper {

    // ── Request → Domain (entrada) ────────────────────────────────────────────

    /**
     * Convierte un {@link UsuarioRequest} en un {@link Usuario} de dominio.
     * Solo mapea los campos que vienen en el request; el estado, fechas e ID
     * los asigna el servicio de aplicación.
     *
     * @param request DTO de entrada validado por Bean Validation.
     * @return Objeto de dominio listo para ser procesado por {@link usuarios.application.service.UsuarioService}.
     */
    public Usuario toDomain(UsuarioRequest request) {
        if (request == null) return null;
        Usuario usuario = new Usuario();
        usuario.setNumeroIdentificacion(request.getNumeroIdentificacion());
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setTelefono(request.getTelefono());
        usuario.setPassword(request.getPassword()); // En texto plano; el servicio lo cifra
        usuario.setNumeroTarjetaProfesional(request.getNumeroTarjetaProfesional());
        return usuario;
    }

    /**
     * Convierte un {@link GrupoRequest} en un {@link Grupo} de dominio.
     * El estado y el ID los asigna el servicio de aplicación.
     *
     * @param request DTO de entrada validado.
     * @return Objeto de dominio listo para ser procesado por {@link usuarios.application.service.GrupoService}.
     */
    public Grupo toDomain(GrupoRequest request) {
        if (request == null) return null;
        Grupo grupo = new Grupo();
        grupo.setNombre(request.getNombre());
        grupo.setDescripcion(request.getDescripcion());
        return grupo;
    }

    // ── Domain → Response (salida) ────────────────────────────────────────────

    /**
     * Convierte un {@link Usuario} de dominio en un {@link UsuarioResponse} para la API REST.
     * Nota: la contraseña {@strong NO} se incluye en la respuesta.
     *
     * @param domain Modelo de dominio con todos los datos del usuario.
     * @return DTO de respuesta seguro para exponer al cliente.
     */
    public UsuarioResponse toResponse(Usuario domain) {
        if (domain == null) return null;
        UsuarioResponse response = new UsuarioResponse();
        response.setId(domain.getId());
        response.setNumeroIdentificacion(domain.getNumeroIdentificacion());
        response.setNombre(domain.getNombre());
        response.setCorreo(domain.getCorreo());
        response.setTelefono(domain.getTelefono());
        response.setNumeroTarjetaProfesional(domain.getNumeroTarjetaProfesional());
        // El enum Estado se convierte a String para la respuesta JSON:
        response.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null);
        response.setFechaCreacion(domain.getFechaCreacion());
        response.setFechaActualizacion(domain.getFechaActualizacion());
        // Mapea recursivamente la lista de grupos con sus privilegios:
        response.setGrupos(toGrupoResponseList(domain.getGrupos()));
        return response;
    }

    /**
     * Convierte un {@link Grupo} de dominio en un {@link GrupoResponse}.
     *
     * @param domain Modelo de dominio del grupo.
     * @return DTO de respuesta con id, nombre, descripción, estado y privilegios.
     */
    public GrupoResponse toResponse(Grupo domain) {
        if (domain == null) return null;
        GrupoResponse response = new GrupoResponse();
        response.setId(domain.getId());
        response.setNombre(domain.getNombre());
        response.setDescripcion(domain.getDescripcion());
        response.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null);
        response.setPrivilegios(toPrivilegioResponseList(domain.getPrivilegios()));
        return response;
    }

    /**
     * Convierte un {@link Privilegio} de dominio en un {@link PrivilegioResponse}.
     *
     * @param domain Modelo de dominio del privilegio.
     * @return DTO de respuesta con código, nombre, acción y recurso.
     */
    public PrivilegioResponse toResponse(Privilegio domain) {
        if (domain == null) return null;
        PrivilegioResponse response = new PrivilegioResponse();
        response.setId(domain.getId());
        response.setCodigo(domain.getCodigo());
        response.setNombre(domain.getNombre());
        response.setDescripcion(domain.getDescripcion());
        response.setAccion(domain.getAccion());
        response.setRecurso(domain.getRecurso());
        return response;
    }

    // ── Colecciones ───────────────────────────────────────────────────────────

    /** Convierte una lista de Grupo de dominio a lista de GrupoResponse. */
    private List<GrupoResponse> toGrupoResponseList(List<Grupo> grupos) {
        if (grupos == null) return Collections.emptyList();
        return grupos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Convierte una lista de Privilegio de dominio a lista de PrivilegioResponse. */
    private List<PrivilegioResponse> toPrivilegioResponseList(List<Privilegio> privilegios) {
        if (privilegios == null) return Collections.emptyList();
        return privilegios.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
