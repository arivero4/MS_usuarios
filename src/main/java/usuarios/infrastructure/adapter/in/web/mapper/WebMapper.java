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
 * Mapper de la capa web: convierte entre DTOs REST y modelos de dominio.
 * No conoce entidades de base de datos ni la capa de persistencia.
 */
@Component
public class WebMapper {

    // ── Request → Domain ─────────────────────────────────────────────────────

    public Usuario toDomain(UsuarioRequest request) {
        if (request == null) return null;
        Usuario usuario = new Usuario();
        usuario.setNumeroIdentificacion(request.getNumeroIdentificacion());
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setTelefono(request.getTelefono());
        usuario.setPassword(request.getPassword());
        usuario.setNumeroTarjetaProfesional(request.getNumeroTarjetaProfesional());
        return usuario;
    }

    public Grupo toDomain(GrupoRequest request) {
        if (request == null) return null;
        Grupo grupo = new Grupo();
        grupo.setNombre(request.getNombre());
        grupo.setDescripcion(request.getDescripcion());
        return grupo;
    }

    // ── Domain → Response ─────────────────────────────────────────────────────

    public UsuarioResponse toResponse(Usuario domain) {
        if (domain == null) return null;
        UsuarioResponse response = new UsuarioResponse();
        response.setId(domain.getId());
        response.setNumeroIdentificacion(domain.getNumeroIdentificacion());
        response.setNombre(domain.getNombre());
        response.setCorreo(domain.getCorreo());
        response.setTelefono(domain.getTelefono());
        response.setNumeroTarjetaProfesional(domain.getNumeroTarjetaProfesional());
        response.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null);
        response.setFechaCreacion(domain.getFechaCreacion());
        response.setFechaActualizacion(domain.getFechaActualizacion());
        response.setGrupos(toGrupoResponseList(domain.getGrupos()));
        return response;
    }

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

    private List<GrupoResponse> toGrupoResponseList(List<Grupo> grupos) {
        if (grupos == null) return Collections.emptyList();
        return grupos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private List<PrivilegioResponse> toPrivilegioResponseList(List<Privilegio> privilegios) {
        if (privilegios == null) return Collections.emptyList();
        return privilegios.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
