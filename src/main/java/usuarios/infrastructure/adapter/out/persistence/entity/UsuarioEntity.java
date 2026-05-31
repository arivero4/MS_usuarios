package usuarios.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de persistencia que representa una fila de la tabla {@code USUARIO} en Oracle.
 *
 * <p>Es un POJO simple sin anotaciones JPA. La persistencia se realiza con
 * {@code JdbcTemplate} en
 * {@link usuarios.infrastructure.adapter.out.persistence.repository.UsuarioRepositoryAdapter}
 * porque Oracle 10g tiene compatibilidad limitada con los dialectos modernos de Hibernate.</p>
 *
 * <p>Diferencias con el modelo de dominio {@link usuarios.domain.model.Usuario}:</p>
 * <ul>
 *   <li>El estado se almacena como {@code String} en lugar del enum {@link usuarios.domain.enums.Estado}.</li>
 *   <li>No tiene métodos de negocio (activar, bloquear, etc.).</li>
 *   <li>La lista de grupos se carga por separado con una consulta JOIN a {@code GRUPOS_USUARIO}.</li>
 * </ul>
 *
 * <p>Columnas de la tabla {@code USUARIO}:</p>
 * <pre>
 *  ID_USUARIO              NUMBER(19)     PK (SEQ_USUARIO)
 *  NUMERO_IDENTIFICACION   VARCHAR2(30)   NOT NULL, UNIQUE
 *  NOMBRE                  VARCHAR2(100)  NOT NULL
 *  CORREO                  VARCHAR2(100)  NOT NULL, UNIQUE
 *  TELEFONO                VARCHAR2(20)   NULL
 *  PASSWORD                VARCHAR2(255)  NOT NULL (BCrypt hash)
 *  NUM_TARJETA_PROFESIONAL VARCHAR2(50)   NULL
 *  ESTADO                  VARCHAR2(30)   NOT NULL CHECK(ACTIVO|INACTIVO|SUSPENDIDO|BLOQUEADO)
 *  FECHA_CREACION          DATE           NOT NULL
 *  FECHA_ACTUALIZACION     DATE           NULL
 * </pre>
 */
public class UsuarioEntity {

    private Long id;
    private String numeroIdentificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private String password;            // Siempre hash BCrypt de 60 caracteres
    private String numeroTarjetaProfesional;
    private String estado;              // String del enum Estado (ACTIVO, BLOQUEADO, etc.)
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    /**
     * Lista de grupos del usuario. No mapea directamente a una columna;
     * se carga mediante JOIN con {@code GRUPOS_USUARIO} y {@code GRUPOS}.
     */
    private List<GrupoEntity> grupos;

    /** Constructor vacío. Inicializa {@code grupos} para evitar NullPointerException. */
    public UsuarioEntity() {
        this.grupos = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNumeroTarjetaProfesional() { return numeroTarjetaProfesional; }
    public void setNumeroTarjetaProfesional(String numeroTarjetaProfesional) { this.numeroTarjetaProfesional = numeroTarjetaProfesional; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<GrupoEntity> getGrupos() { return grupos; }
    public void setGrupos(List<GrupoEntity> grupos) { this.grupos = grupos; }
}
