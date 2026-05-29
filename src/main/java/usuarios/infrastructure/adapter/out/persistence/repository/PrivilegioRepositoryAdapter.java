package usuarios.infrastructure.adapter.out.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import usuarios.application.port.out.PrivilegioRepositoryPort;
import usuarios.domain.model.Privilegio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: implementa PrivilegioRepositoryPort usando JDBC sobre Oracle 10g.
 */
@Repository
public class PrivilegioRepositoryAdapter implements PrivilegioRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public PrivilegioRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_BY_ID =
            "SELECT ID, CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO FROM PRIVILEGIOS WHERE ID = ?";

    private static final String SELECT_ALL =
            "SELECT ID, CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO FROM PRIVILEGIOS ORDER BY ID";

    private static final String EXISTS_BY_CODIGO =
            "SELECT COUNT(1) FROM PRIVILEGIOS WHERE CODIGO = ?";

    @Override
    public Optional<Privilegio> buscarPorId(Long id) {
        List<Privilegio> result = jdbcTemplate.query(SELECT_BY_ID, privilegioRowMapper(), id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<Privilegio> buscarTodos() {
        return jdbcTemplate.query(SELECT_ALL, privilegioRowMapper());
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_BY_CODIGO, Integer.class, codigo);
        return count != null && count > 0;
    }

    private static final RowMapper<Privilegio> PRIVILEGIO_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Privilegio mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new Privilegio(
                    rs.getLong("ID"),
                    rs.getString("CODIGO"),
                    rs.getString("NOMBRE"),
                    rs.getString("DESCRIPCION"),
                    rs.getString("ACCION"),
                    rs.getString("RECURSO")
            );
        }
    };

    private static RowMapper<Privilegio> privilegioRowMapper() {
        return PRIVILEGIO_ROW_MAPPER;
    }
}
