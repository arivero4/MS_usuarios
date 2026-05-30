package usuarios.infrastructure.adapter.out.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import usuarios.application.port.out.GrupoRepositoryPort;
import usuarios.domain.model.Grupo;
import usuarios.infrastructure.adapter.out.persistence.entity.GrupoEntity;
import usuarios.infrastructure.adapter.out.persistence.entity.PrivilegioEntity;
import usuarios.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GrupoRepositoryAdapter implements GrupoRepositoryPort {

    private final JdbcTemplate jdbcTemplate;
    private final PersistenceMapper mapper;

    public GrupoRepositoryAdapter(JdbcTemplate jdbcTemplate, PersistenceMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper       = mapper;
    }

    // ── SQL ───────────────────────────────────────────────────────────────────

    private static final String INSERT      = "INSERT INTO GRUPOS (NOMBRE,DESCRIPCION,ESTADO) VALUES (?,?,?)";
    private static final String SELECT_BASE = "SELECT ID,NOMBRE,DESCRIPCION,ESTADO FROM GRUPOS";
    private static final String UPDATE      = "UPDATE GRUPOS SET NOMBRE=?,DESCRIPCION=?,ESTADO=? WHERE ID=?";
    private static final String DELETE      = "DELETE FROM GRUPOS WHERE ID=?";
    private static final String EXISTS_NOMBRE = "SELECT COUNT(1) FROM GRUPOS WHERE NOMBRE=?";

    private static final String INSERT_GRP_PRV    = "INSERT INTO PRIVILEGIO_GRUPO (ID_GRUPO,ID_PRIVILEGIO) VALUES (?,?)";
    private static final String DELETE_GRP_PRV    = "DELETE FROM PRIVILEGIO_GRUPO WHERE ID_GRUPO=?";

    private static final String SELECT_PRIVILEGIOS =
            "SELECT P.ID,P.CODIGO,P.NOMBRE,P.DESCRIPCION,P.ACCION,P.RECURSO FROM PRIVILEGIOS P " +
            "JOIN PRIVILEGIO_GRUPO PG ON P.ID=PG.ID_PRIVILEGIO WHERE PG.ID_GRUPO=?";

    // ── Operaciones ───────────────────────────────────────────────────────────

    @Override
    public Grupo guardar(Grupo grupo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"});
            ps.setString(1, grupo.getNombre());
            ps.setString(2, grupo.getDescripcion());
            ps.setString(3, grupo.getEstado() != null ? grupo.getEstado().name() : null);
            return ps;
        }, keyHolder);
        grupo.setId(keyHolder.getKey().longValue());
        return grupo;
    }

    @Override
    public Optional<Grupo> buscarPorId(Long id) {
        List<GrupoEntity> rows = jdbcTemplate.query(SELECT_BASE + " WHERE ID=?", grupoRowMapper(), id);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapper.toDomain(cargarPrivilegios(rows.get(0))));
    }

    @Override
    public List<Grupo> buscarTodos() {
        return jdbcTemplate.query(SELECT_BASE + " ORDER BY NOMBRE", grupoRowMapper()).stream()
                .map(e -> mapper.toDomain(cargarPrivilegios(e)))
                .collect(Collectors.toList());
    }

    @Override
    public Grupo actualizar(Grupo grupo) {
        jdbcTemplate.update(UPDATE,
                grupo.getNombre(),
                grupo.getDescripcion(),
                grupo.getEstado() != null ? grupo.getEstado().name() : null,
                grupo.getId());
        jdbcTemplate.update(DELETE_GRP_PRV, grupo.getId());
        if (grupo.getPrivilegios() != null) {
            grupo.getPrivilegios().forEach(p ->
                    jdbcTemplate.update(INSERT_GRP_PRV, grupo.getId(), p.getId()));
        }
        return grupo;
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update(DELETE_GRP_PRV, id);
        jdbcTemplate.update(DELETE, id);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_NOMBRE, Integer.class, nombre);
        return count != null && count > 0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private GrupoEntity cargarPrivilegios(GrupoEntity entity) {
        entity.setPrivilegios(jdbcTemplate.query(SELECT_PRIVILEGIOS, new RowMapper<>() {
            @Override
            public PrivilegioEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                PrivilegioEntity p = new PrivilegioEntity();
                p.setId(rs.getLong("ID"));
                p.setCodigo(rs.getString("CODIGO"));
                p.setNombre(rs.getString("NOMBRE"));
                p.setDescripcion(rs.getString("DESCRIPCION"));
                p.setAccion(rs.getString("ACCION"));
                p.setRecurso(rs.getString("RECURSO"));
                return p;
            }
        }, entity.getId()));
        return entity;
    }

    private static final RowMapper<GrupoEntity> GRUPO_ROW_MAPPER = new RowMapper<>() {
        @Override
        public GrupoEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            GrupoEntity e = new GrupoEntity();
            e.setId(rs.getLong("ID"));
            e.setNombre(rs.getString("NOMBRE"));
            e.setDescripcion(rs.getString("DESCRIPCION"));
            e.setEstado(rs.getString("ESTADO"));
            return e;
        }
    };

    private static RowMapper<GrupoEntity> grupoRowMapper() {
        return GRUPO_ROW_MAPPER;
    }
}
