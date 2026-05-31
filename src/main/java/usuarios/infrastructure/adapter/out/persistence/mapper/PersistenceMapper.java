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
 * Mapper de la capa de persistencia (adaptador de salida).
 *
 * <p>Traduce entre las entidades Java planas que representan las tablas Oracle
 * ({@link UsuarioEntity}, {@link GrupoEntity}, {@link PrivilegioEntity}) y
 * los modelos de dominio puros ({@link Usuario}, {@link Grupo}, {@link Privilegio}).</p>
 *
 * <p>Diferencias clave entre entidades y modelos de dominio:</p>
 * <ul>
 *   <li>Las entidades representan filas de tabla; el dominio representa conceptos de negocio.</li>
 *   <li>Las entidades almacenan el estado como {@code String} (para evitar depender del enum
 *       en la capa de persistencia); el dominio usa el enum {@link Estado}.</li>
 *   <li>Las entidades no tienen métodos de negocio; el dominio sí ({@code activar()}, etc.).</li>
 * </ul>
 */
@Component
public class PersistenceMapper {

    // ── UsuarioEntity ↔ Usuario ───────────────────────────────────────────────

    /**
     * Convierte una {@link UsuarioEntity} (fila de BD) en un {@link Usuario} de dominio.
     * Mapea el estado de String a enum y construye la lista de grupos recursivamente.
     *
     * @param entity Fila de la tabla {@code USUARIO} con sus grupos cargados.
     * @return Objeto de dominio con todos los datos y comportamientos de negocio.
     */
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
                parseEstado(entity.getEstado()), // String → enum
                entity.getFechaCreacion(),
                entity.getFechaActualizacion(),
                toGrupoDomainList(entity.getGrupos()) // Mapea recursivamente los grupos
        );
    }

    /**
     * Convierte un {@link Usuario} de dominio en una {@link UsuarioEntity} para persistir.
     * Mapea el estado de enum a String. No incluye la lista de grupos en la entidad
     * porque se gestiona por separado via SQL en {@code GRUPOS_USUARIO}.
     *
     * @param domain Modelo de dominio con los datos a persistir.
     * @return Entidad lista para ser usada por {@link usuarios.infrastructure.adapter.out.persistence.repository.UsuarioRepositoryAdapter}.
     */
    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(domain.getId());
        entity.setNumeroIdentificacion(domain.getNumeroIdentificacion());
        entity.setNombre(domain.getNombre());
        entity.setCorreo(domain.getCorreo());
        entity.setTelefono(domain.getTelefono());
        entity.setPassword(domain.getPassword()); // Ya viene cifrado con BCrypt
        entity.setNumeroTarjetaProfesional(domain.getNumeroTarjetaProfesional());
        entity.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null); // enum → String
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaActualizacion(domain.getFechaActualizacion());
        return entity;
    }

    // ── GrupoEntity ↔ Grupo ───────────────────────────────────────────────────

    /**
     * Convierte una {@link GrupoEntity} en un {@link Grupo} de dominio.
     *
     * @param entity Fila de la tabla {@code GRUPOS} con sus privilegios cargados.
     * @return Objeto de dominio del grupo.
     */
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

    /**
     * Convierte un {@link Grupo} de dominio en una {@link GrupoEntity}.
     * No incluye la lista de privilegios porque se gestiona por separado
     * via SQL en {@code PRIVILEGIO_GRUPO}.
     *
     * @param domain Modelo de dominio del grupo.
     * @return Entidad para persistencia.
     */
    public GrupoEntity toEntity(Grupo domain) {
        if (domain == null) return null;
        GrupoEntity entity = new GrupoEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setEstado(domain.getEstado() != null ? domain.getEstado().name() : null);
        return entity;
    }

    // ── PrivilegioEntity ↔ Privilegio ─────────────────────────────────────────

    /**
     * Convierte una {@link PrivilegioEntity} en un {@link Privilegio} de dominio.
     *
     * @param entity Fila de la tabla {@code PRIVILEGIOS}.
     * @return Objeto de dominio del privilegio.
     */
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

    /**
     * Convierte una lista de {@link GrupoEntity} en lista de {@link Grupo} de dominio.
     * Retorna lista vacía (no null) si la entrada es null.
     */
    public List<Grupo> toGrupoDomainList(List<GrupoEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * Convierte una lista de {@link PrivilegioEntity} en lista de {@link Privilegio} de dominio.
     * Retorna lista vacía (no null) si la entrada es null.
     */
    public List<Privilegio> toPrivilegioDomainList(List<PrivilegioEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Convierte el String del estado almacenado en BD al enum {@link Estado}.
     * Retorna null si el valor es null (evita NullPointerException en {@code valueOf}).
     *
     * @param valor String con el nombre del estado (ej: "ACTIVO", "BLOQUEADO").
     * @return El enum {@link Estado} correspondiente, o null.
     * @throws IllegalArgumentException si el valor no corresponde a ningún estado del enum.
     */
    private Estado parseEstado(String valor) {
        return valor != null ? Estado.valueOf(valor) : null;
    }
}
