package usuarios.infrastructure.adapter.out.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import usuarios.application.port.out.UsuarioRepositoryPort;
import usuarios.domain.model.Usuario;
import usuarios.infrastructure.adapter.out.persistence.entity.GrupoEntity;
import usuarios.infrastructure.adapter.out.persistence.entity.PrivilegioEntity;
import usuarios.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import usuarios.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final JdbcTemplate jdbcTemplate;
    private final PersistenceMapper mapper;

    public UsuarioRepositoryAdapter(JdbcTemplate jdbcTemplate, PersistenceMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper       = mapper;
    }

    // ── SQL ───────────────────────────────────────────────────────────────────

    private static final String INSERT =
            "INSERT INTO USUARIOS (ID,NUMERO_IDENTIFICACION,NOMBRE,CORREO,TELEFONO," +
            "PASSWORD,NUMERO_TARJETA_PROFESIONAL,ESTADO,FECHA_CREACION,FECHA_ACTUALIZACION) " +
            "VALUES (SEQ_USUARIOS.NEXTVAL,?,?,?,?,?,?,?,?,?)";

    private static final String SELECT_BASE =
            "SELECT ID,NUMERO_IDENTIFICACION,NOMBRE,CORREO,TELEFONO," +
            "PASSWORD,NUMERO_TARJETA_PROFESIONAL,ESTADO,FECHA_CREACION,FECHA_ACTUALIZACION FROM USUARIOS";

    private static final String UPDATE =
            "UPDATE USUARIOS SET NOMBRE=?,TELEFONO=?,NUMERO_TARJETA_PROFESIONAL=?," +
            "ESTADO=?,FECHA_ACTUALIZACION=? WHERE ID=?";

    private static final String DELETE              = "DELETE FROM USUARIOS WHERE ID=?";
    private static final String EXISTS_CORREO       = "SELECT COUNT(1) FROM USUARIOS WHERE CORREO=?";
    private static final String EXISTS_NUM_ID       = "SELECT COUNT(1) FROM USUARIOS WHERE NUMERO_IDENTIFICACION=?";
    private static final String INSERT_USR_GRP      = "INSERT INTO USUARIO_GRUPO (USUARIO_ID,GRUPO_ID) VALUES (?,?)";
    private static final String DELETE_USR_GRP_ALL  = "DELETE FROM USUARIO_GRUPO WHERE USUARIO_ID=?";

    private static final String SELECT_GRUPOS =
            "SELECT G.ID,G.NOMBRE,G.DESCRIPCION,G.ESTADO FROM GRUPOS G " +
            "JOIN USUARIO_GRUPO UG ON G.ID=UG.GRUPO_ID WHERE UG.USUARIO_ID=?";

    private static final String SELECT_PRIVILEGIOS =
            "SELECT P.ID,P.CODIGO,P.NOMBRE,P.DESCRIPCION,P.ACCION,P.RECURSO FROM PRIVILEGIOS P " +
            "JOIN GRUPO_PRIVILEGIO GP ON P.ID=GP.PRIVILEGIO_ID WHERE GP.GRUPO_ID=?";

    // ── Operaciones ───────────────────────────────────────────────────────────

    @Override
    public Usuario guardar(Usuario usuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"});
            ps.setString(1, usuario.getNumeroIdentificacion());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getTelefono());
            ps.setString(5, usuario.getPassword());
            ps.setString(6, usuario.getNumeroTarjetaProfesional());
            ps.setString(7, usuario.getEstado() != null ? usuario.getEstado().name() : null);
            ps.setTimestamp(8, Timestamp.valueOf(usuario.getFechaCreacion()));
            ps.setTimestamp(9, Timestamp.valueOf(usuario.getFechaActualizacion()));
            return ps;
        }, keyHolder);
        usuario.setId(keyHolder.getKey().longValue());
        sincronizarGrupos(usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        List<UsuarioEntity> rows = jdbcTemplate.query(SELECT_BASE + " WHERE ID=?", usuarioRowMapper(), id);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapper.toDomain(cargarRelaciones(rows.get(0))));
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        List<UsuarioEntity> rows = jdbcTemplate.query(SELECT_BASE + " WHERE CORREO=?", usuarioRowMapper(), correo);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapper.toDomain(cargarRelaciones(rows.get(0))));
    }

    @Override
    public Optional<Usuario> buscarPorNumeroIdentificacion(String numeroIdentificacion) {
        List<UsuarioEntity> rows = jdbcTemplate.query(
                SELECT_BASE + " WHERE NUMERO_IDENTIFICACION=?", usuarioRowMapper(), numeroIdentificacion);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapper.toDomain(cargarRelaciones(rows.get(0))));
    }

    @Override
    public List<Usuario> buscarTodos() {
        return jdbcTemplate.query(SELECT_BASE + " ORDER BY ID", usuarioRowMapper()).stream()
                .map(e -> mapper.toDomain(cargarRelaciones(e)))
                .collect(Collectors.toList());
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        jdbcTemplate.update(UPDATE,
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getNumeroTarjetaProfesional(),
                usuario.getEstado() != null ? usuario.getEstado().name() : null,
                Timestamp.valueOf(LocalDateTime.now()),
                usuario.getId());
        jdbcTemplate.update(DELETE_USR_GRP_ALL, usuario.getId());
        sincronizarGrupos(usuario);
        return usuario;
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update(DELETE_USR_GRP_ALL, id);
        jdbcTemplate.update(DELETE, id);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_CORREO, Integer.class, correo);
        return count != null && count > 0;
    }

    @Override
    public boolean existePorNumeroIdentificacion(String numero) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_NUM_ID, Integer.class, numero);
        return count != null && count > 0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sincronizarGrupos(Usuario usuario) {
        if (usuario.getGrupos() == null) return;
        usuario.getGrupos().forEach(g ->
                jdbcTemplate.update(INSERT_USR_GRP, usuario.getId(), g.getId()));
    }

    private UsuarioEntity cargarRelaciones(UsuarioEntity entity) {
        entity.setGrupos(cargarGrupos(entity.getId()));
        return entity;
    }

    private List<GrupoEntity> cargarGrupos(Long usuarioId) {
        return jdbcTemplate.query(SELECT_GRUPOS, new RowMapper<>() {
            @Override
            public GrupoEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                GrupoEntity g = new GrupoEntity();
                g.setId(rs.getLong("ID"));
                g.setNombre(rs.getString("NOMBRE"));
                g.setDescripcion(rs.getString("DESCRIPCION"));
                g.setEstado(rs.getString("ESTADO"));
                g.setPrivilegios(cargarPrivilegios(g.getId()));
                return g;
            }
        }, usuarioId);
    }

    private List<PrivilegioEntity> cargarPrivilegios(Long grupoId) {
        return jdbcTemplate.query(SELECT_PRIVILEGIOS, new RowMapper<>() {
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
        }, grupoId);
    }

    private static final RowMapper<UsuarioEntity> USUARIO_ROW_MAPPER = new RowMapper<>() {
        @Override
        public UsuarioEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            UsuarioEntity e = new UsuarioEntity();
            e.setId(rs.getLong("ID"));
            e.setNumeroIdentificacion(rs.getString("NUMERO_IDENTIFICACION"));
            e.setNombre(rs.getString("NOMBRE"));
            e.setCorreo(rs.getString("CORREO"));
            e.setTelefono(rs.getString("TELEFONO"));
            e.setPassword(rs.getString("PASSWORD"));
            e.setNumeroTarjetaProfesional(rs.getString("NUMERO_TARJETA_PROFESIONAL"));
            e.setEstado(rs.getString("ESTADO"));
            Timestamp fc = rs.getTimestamp("FECHA_CREACION");
            e.setFechaCreacion(fc != null ? fc.toLocalDateTime() : null);
            Timestamp fa = rs.getTimestamp("FECHA_ACTUALIZACION");
            e.setFechaActualizacion(fa != null ? fa.toLocalDateTime() : null);
            return e;
        }
    };

    private static RowMapper<UsuarioEntity> usuarioRowMapper() {
        return USUARIO_ROW_MAPPER;
    }
}
