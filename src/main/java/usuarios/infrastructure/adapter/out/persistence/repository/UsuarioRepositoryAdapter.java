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

/**
 * Adaptador de salida que implementa {@link UsuarioRepositoryPort} usando
 * {@code JdbcTemplate} de Spring contra Oracle 10g.
 *
 * <p>Se eligió JDBC puro (sin JPA/Hibernate) por la compatibilidad limitada
 * de Hibernate con Oracle 10g. Esto también ofrece control total sobre el SQL.</p>
 *
 * <p>Estrategia de carga de relaciones (N+1 consciente):</p>
 * <ul>
 *   <li>La consulta principal carga solo la tabla {@code USUARIO}.</li>
 *   <li>Por cada usuario se ejecuta una consulta adicional para sus grupos
 *       (JOIN {@code GRUPOS_USUARIO} ↔ {@code GRUPOS}).</li>
 *   <li>Por cada grupo se ejecuta una consulta para sus privilegios
 *       (JOIN {@code PRIVILEGIO_GRUPO} ↔ {@code PRIVILEGIOS}).</li>
 * </ul>
 * <p>En sistemas con pocos usuarios (como este) el N+1 es aceptable.
 * Para grandes volúmenes se reemplazaría por un JOIN en una sola consulta.</p>
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final JdbcTemplate jdbcTemplate;
    private final PersistenceMapper mapper;

    public UsuarioRepositoryAdapter(JdbcTemplate jdbcTemplate, PersistenceMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper       = mapper;
    }

    // ── Sentencias SQL ────────────────────────────────────────────────────────

    /**
     * INSERT que usa {@code SEQ_USUARIO.NEXTVAL} de Oracle para generar el ID.
     * Los 9 parámetros corresponden a los campos en orden (excluyendo ID que lo genera Oracle).
     */
    private static final String INSERT =
            "INSERT INTO USUARIO (ID_USUARIO,NUMERO_IDENTIFICACION,NOMBRE,CORREO,TELEFONO," +
            "PASSWORD,NUM_TARJETA_PROFESIONAL,ESTADO,FECHA_CREACION,FECHA_ACTUALIZACION) " +
            "VALUES (SEQ_USUARIO.NEXTVAL,?,?,?,?,?,?,?,?,?)";

    /** SELECT base sin WHERE para reusar con distintos filtros. */
    private static final String SELECT_BASE =
            "SELECT ID_USUARIO,NUMERO_IDENTIFICACION,NOMBRE,CORREO,TELEFONO," +
            "PASSWORD,NUM_TARJETA_PROFESIONAL,ESTADO,FECHA_CREACION,FECHA_ACTUALIZACION FROM USUARIO";

    /** UPDATE que modifica solo los campos editables (no correo ni contraseña). */
    private static final String UPDATE =
            "UPDATE USUARIO SET NOMBRE=?,TELEFONO=?,NUM_TARJETA_PROFESIONAL=?," +
            "ESTADO=?,FECHA_ACTUALIZACION=? WHERE ID_USUARIO=?";

    private static final String DELETE             = "DELETE FROM USUARIO WHERE ID_USUARIO=?";
    private static final String EXISTS_CORREO      = "SELECT COUNT(1) FROM USUARIO WHERE CORREO=?";
    private static final String EXISTS_NUM_ID      = "SELECT COUNT(1) FROM USUARIO WHERE NUMERO_IDENTIFICACION=?";

    /** INSERT en la tabla de relación N:M usuario-grupo. */
    private static final String INSERT_USR_GRP     = "INSERT INTO GRUPOS_USUARIO (ID_USUARIO,ID_GRUPO) VALUES (?,?)";
    /** Elimina TODOS los grupos de un usuario (para luego reinsertar los actuales). */
    private static final String DELETE_USR_GRP_ALL = "DELETE FROM GRUPOS_USUARIO WHERE ID_USUARIO=?";

    /** Consulta los grupos de un usuario via JOIN con la tabla de relación. */
    private static final String SELECT_GRUPOS =
            "SELECT G.ID,G.NOMBRE,G.DESCRIPCION,G.ESTADO FROM GRUPOS G " +
            "JOIN GRUPOS_USUARIO GU ON G.ID=GU.ID_GRUPO WHERE GU.ID_USUARIO=?";

    /** Consulta los privilegios de un grupo via JOIN con la tabla de relación. */
    private static final String SELECT_PRIVILEGIOS =
            "SELECT P.ID,P.CODIGO,P.NOMBRE,P.DESCRIPCION,P.ACCION,P.RECURSO FROM PRIVILEGIOS P " +
            "JOIN PRIVILEGIO_GRUPO PG ON P.ID=PG.ID_PRIVILEGIO WHERE PG.ID_GRUPO=?";

    // ── Implementación del puerto ─────────────────────────────────────────────

    /**
     * Persiste un nuevo usuario. Usa {@code KeyHolder} para recuperar el ID
     * generado por la secuencia Oracle después del INSERT.
     * Luego sincroniza los grupos en {@code GRUPOS_USUARIO}.
     */
    @Override
    public Usuario guardar(Usuario usuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            // Se solicita la columna ID_USUARIO para que Oracle devuelva el valor generado:
            PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID_USUARIO"});
            ps.setString(1, usuario.getNumeroIdentificacion());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getTelefono());
            ps.setString(5, usuario.getPassword()); // Ya cifrado con BCrypt
            ps.setString(6, usuario.getNumeroTarjetaProfesional());
            ps.setString(7, usuario.getEstado() != null ? usuario.getEstado().name() : null);
            ps.setTimestamp(8, Timestamp.valueOf(usuario.getFechaCreacion()));
            ps.setTimestamp(9, Timestamp.valueOf(usuario.getFechaActualizacion()));
            return ps;
        }, keyHolder);
        // Asigna el ID generado por Oracle al objeto de dominio:
        usuario.setId(keyHolder.getKey().longValue());
        sincronizarGrupos(usuario); // Inserta en GRUPOS_USUARIO
        return usuario;
    }

    /**
     * Busca por ID y carga grupos + privilegios (N+1).
     * Usa lista para manejar el caso de no encontrado sin excepción.
     */
    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        List<UsuarioEntity> rows = jdbcTemplate.query(SELECT_BASE + " WHERE ID_USUARIO=?", usuarioRowMapper(), id);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapper.toDomain(cargarRelaciones(rows.get(0))));
    }

    /**
     * Busca por correo y carga relaciones. Usado en el login y en el JwtAuthFilter.
     */
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

    /** Lista todos los usuarios ordenados por ID, cargando relaciones para cada uno. */
    @Override
    public List<Usuario> buscarTodos() {
        return jdbcTemplate.query(SELECT_BASE + " ORDER BY ID_USUARIO", usuarioRowMapper()).stream()
                .map(e -> mapper.toDomain(cargarRelaciones(e)))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza campos del usuario y resincroniza sus grupos:
     * <ol>
     *   <li>UPDATE en tabla USUARIO (solo campos editables).</li>
     *   <li>DELETE de todos los registros en GRUPOS_USUARIO para este usuario.</li>
     *   <li>INSERT de los grupos actuales en GRUPOS_USUARIO.</li>
     * </ol>
     * Esta estrategia "borrar y reinsertar" es simple y correcta para listas pequeñas.
     */
    @Override
    public Usuario actualizar(Usuario usuario) {
        jdbcTemplate.update(UPDATE,
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getNumeroTarjetaProfesional(),
                usuario.getEstado() != null ? usuario.getEstado().name() : null,
                Timestamp.valueOf(LocalDateTime.now()),
                usuario.getId());
        jdbcTemplate.update(DELETE_USR_GRP_ALL, usuario.getId()); // Elimina asignaciones previas
        sincronizarGrupos(usuario);                               // Reinserta las actuales
        return usuario;
    }

    /**
     * Elimina el usuario y sus relaciones en GRUPOS_USUARIO.
     * El orden es importante: primero las FKs, luego el registro principal.
     */
    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update(DELETE_USR_GRP_ALL, id); // Elimina FKs primero
        jdbcTemplate.update(DELETE, id);              // Luego el registro
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

    // ── Métodos auxiliares privados ───────────────────────────────────────────

    /**
     * Inserta todos los grupos del usuario en la tabla de relación {@code GRUPOS_USUARIO}.
     * Si la lista es null, no hace nada.
     */
    private void sincronizarGrupos(Usuario usuario) {
        if (usuario.getGrupos() == null) return;
        usuario.getGrupos().forEach(g ->
                jdbcTemplate.update(INSERT_USR_GRP, usuario.getId(), g.getId()));
    }

    /**
     * Carga los grupos (y sus privilegios) de un usuario y los asigna a la entidad.
     * Genera N+1 consultas pero es válido para este volumen de datos.
     */
    private UsuarioEntity cargarRelaciones(UsuarioEntity entity) {
        entity.setGrupos(cargarGrupos(entity.getId()));
        return entity;
    }

    /** Ejecuta SELECT_GRUPOS y por cada grupo carga sus privilegios. */
    private List<GrupoEntity> cargarGrupos(Long usuarioId) {
        return jdbcTemplate.query(SELECT_GRUPOS, new RowMapper<>() {
            @Override
            public GrupoEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                GrupoEntity g = new GrupoEntity();
                g.setId(rs.getLong("ID"));
                g.setNombre(rs.getString("NOMBRE"));
                g.setDescripcion(rs.getString("DESCRIPCION"));
                g.setEstado(rs.getString("ESTADO"));
                g.setPrivilegios(cargarPrivilegios(g.getId())); // N+1 por grupo
                return g;
            }
        }, usuarioId);
    }

    /** Ejecuta SELECT_PRIVILEGIOS para un grupo específico. */
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

    /**
     * {@link RowMapper} estático para mapear ResultSet → UsuarioEntity.
     * Estático (final) para evitar crear una nueva instancia por cada consulta.
     * Maneja correctamente los Timestamp de Oracle convirtiéndolos a LocalDateTime.
     */
    private static final RowMapper<UsuarioEntity> USUARIO_ROW_MAPPER = new RowMapper<>() {
        @Override
        public UsuarioEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            UsuarioEntity e = new UsuarioEntity();
            e.setId(rs.getLong("ID_USUARIO"));
            e.setNumeroIdentificacion(rs.getString("NUMERO_IDENTIFICACION"));
            e.setNombre(rs.getString("NOMBRE"));
            e.setCorreo(rs.getString("CORREO"));
            e.setTelefono(rs.getString("TELEFONO"));
            e.setPassword(rs.getString("PASSWORD"));
            e.setNumeroTarjetaProfesional(rs.getString("NUM_TARJETA_PROFESIONAL"));
            e.setEstado(rs.getString("ESTADO"));
            // Oracle DATE se convierte a Timestamp y luego a LocalDateTime:
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
