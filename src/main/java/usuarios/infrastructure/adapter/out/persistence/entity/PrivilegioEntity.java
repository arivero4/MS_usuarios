package usuarios.infrastructure.adapter.out.persistence.entity;

/**
 * Entidad de persistencia que representa una fila de la tabla {@code PRIVILEGIOS} en Oracle.
 *
 * <p>Los privilegios son datos de catálogo insertados en la migración
 * {@code V2__create_grupo.sql}. Raramente se modifican en tiempo de ejecución.</p>
 *
 * <p>Columnas de la tabla {@code PRIVILEGIOS}:</p>
 * <pre>
 *  ID          NUMBER(19)     PK (SEQ_PRIVILEGIOS)
 *  CODIGO      VARCHAR2(50)   NOT NULL, UNIQUE (UK_PRIVILEGIOS_CODIGO)
 *  NOMBRE      VARCHAR2(100)  NOT NULL
 *  DESCRIPCION VARCHAR2(500)  NULL
 *  ACCION      VARCHAR2(100)  NOT NULL  (GET, POST, PUT, PATCH, DELETE, ALL)
 *  RECURSO     VARCHAR2(200)  NOT NULL  (ej: /api/v1/usuarios)
 * </pre>
 *
 * <p>Datos iniciales del catálogo:</p>
 * <pre>
 *  USR_CREAR    → POST   /api/v1/usuarios
 *  USR_LEER     → GET    /api/v1/usuarios
 *  USR_EDITAR   → PUT    /api/v1/usuarios
 *  USR_ELIMINAR → DELETE /api/v1/usuarios
 *  GRP_ADMIN    → ALL    /api/v1/grupos
 * </pre>
 */
public class PrivilegioEntity {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String accion;
    private String recurso;

    public PrivilegioEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }
}
