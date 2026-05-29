package usuarios.infrastructure.adapter.out.persistence.mapper;

import org.springframework.stereotype.Component;
import usuarios.domain.enums.Estado;
import usuarios.domain.model.Grupo;
import usuarios.domain.model.Privilegio;
import usuarios.domain.model.Usuario;
import usuarios.infrastructure.adapter.out.persistence.entity.GrupoEntity;
import usuarios.infrastructure.adapter.out.persistence.entity.PrivilegioEntity;
import usuarios.infrastructure.adapter.out.persistence.entity.UsuarioEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de la capa de persistencia: convierte entre entidades Oracle y modelos de dominio.
 * No conoce DTOs REST ni la capa web.
 */
@Component
public class PersistenceMapper {

    // ── Usuario ──────────────────────────────────────────────────────────────

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;
        return new Usuario(
                entity.getId(),
                entity.getNumeroIdentificacion(),
                entity.getNombre(),
                entity.getCorreo(),
                entity.getTelefono(),
                entity.getPassword(),
                entity.getNumeroTarjetaProfesional(),
                parseEstado(entity.getEstado()),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion(),
                toGrupoDomainList(entity.getGrupos())
        );
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(domain.getId());
        entity.setNumeroIdentificacion(domain.getNumeroIdentificacion());
        entity.setNombre(domain.getNombre());
        entity.setCorreo(domain.getCorreo());
        entity.setTelefono(domain.getTelefono());
        entity.setPassword(domain.getPassword());
        entity.setNumeroTarjetaProfesional(domain.getNumeroTarjetaProfesional());
        entity.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null);
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaActualizacion(domain.getFechaActualizacion());
        return entity;
    }

    // ── Grupo ─────────────────────────────────────────────────────────────────

    public Grupo toDomain(GrupoEntity entity) {
        if (entity == null) return null;
        return new Grupo(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                parseEstado(entity.getEstado()),
                toPrivilegioDomainList(entity.getPrivilegios())
        );
    }

    public GrupoEntity toEntity(Grupo domain) {
        if (domain == null) return null;
        GrupoEntity entity = new GrupoEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null);
        return entity;
    }

    // ── Privilegio ────────────────────────────────────────────────────────────

    public Privilegio toDomain(PrivilegioEntity entity) {
        if (entity == null) return null;
        return new Privilegio(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getAccion(),
                entity.getRecurso()
        );
    }

    // ── Colecciones ───────────────────────────────────────────────────────────

    public List<Grupo> toGrupoDomainList(List<GrupoEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    public List<Privilegio> toPrivilegioDomainList(List<PrivilegioEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private Estado parseEstado(String valor) {
        return valor != null ? Estado.valueOf(valor) : null;
    }
}
