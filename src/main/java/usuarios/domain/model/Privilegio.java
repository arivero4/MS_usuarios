package usuarios.domain.model;

/**
 * Entidad de dominio que representa un privilegio (permiso) del sistema.
 *
 * <p>Un privilegio define una acción concreta que puede realizarse sobre un
 * recurso específico. Los privilegios se asignan a {@link Grupo}s; los usuarios
 * heredan los privilegios de sus grupos.</p>
 *
 * <p>Se persiste en la tabla {@code PRIVILEGIOS}. Datos iniciales son
 * insertados por la migración {@code V2__create_grupo.sql}:</p>
 * <ul>
 *   <li>{@code USR_CREAR}    – POST /api/v1/usuarios</li>
 *   <li>{@code USR_LEER}     – GET  /api/v1/usuarios</li>
 *   <li>{@code USR_EDITAR}   – PUT  /api/v1/usuarios</li>
 *   <li>{@code USR_ELIMINAR} – DELETE /api/v1/usuarios</li>
 *   <li>{@code GRP_ADMIN}    – ALL  /api/v1/grupos</li>
 * </ul>
 */
public class Privilegio {

    /** Identificador único, generado por la secuencia {@code SEQ_PRIVILEGIOS} de Oracle. */
    private Long id;

    /**
     * Código único del privilegio (constraint {@code UK_PRIVILEGIOS_CODIGO}).
     * Convenio de nomenclatura: {@code RECURSO_ACCION} (p. ej. {@code USR_CREAR}).
     */
    private String codigo;

    /** Nombre legible del privilegio (p. ej. "Crear Usuario"). */
    private String nombre;

    /** Descripción del permiso que otorga este privilegio. */
    private String descripcion;

    /**
     * Verbo HTTP que habilita este privilegio.
     * Valores típicos: GET, POST, PUT, PATCH, DELETE, ALL.
     */
    private String accion;

    /**
     * Ruta del recurso protegido por este privilegio.
     * Ejemplo: {@code /api/v1/usuarios}.
     */
    private String recurso;

    /** Constructor vacío requerido por los mappers de persistencia. */
    public Privilegio() {}

    /**
     * Constructor completo. Utilizado por
     * {@link usuarios.infrastructure.adapter.out.persistence.mapper.PersistenceMapper#toDomain(usuarios.infrastructure.adapter.out.persistence.entity.PrivilegioEntity)}.
     */
    public Privilegio(Long id, String codigo, String nombre, String descripcion,
                      String accion, String recurso) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.accion = accion;
        this.recurso = recurso;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

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
