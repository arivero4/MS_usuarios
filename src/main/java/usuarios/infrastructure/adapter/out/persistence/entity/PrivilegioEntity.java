package usuarios.infrastructure.adapter.out.persistence.entity;

/**
 * Representa la tabla PRIVILEGIOS en Oracle 10g.
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
