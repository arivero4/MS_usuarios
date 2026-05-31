package usuarios.infrastructure.adapter.in.web.dto;

/**
 * DTO de salida que representa un privilegio en las respuestas REST.
 *
 * <p>Aparece anidado dentro de {@link GrupoResponse#getPrivilegios()}.</p>
 *
 * <p>Ejemplo de JSON de salida:</p>
 * <pre>
 * {
 *   "id":          1,
 *   "codigo":      "USR_CREAR",
 *   "nombre":      "Crear Usuario",
 *   "descripcion": "Permite crear nuevos usuarios en el sistema",
 *   "accion":      "POST",
 *   "recurso":     "/api/v1/usuarios"
 * }
 * </pre>
 */
public class PrivilegioResponse {

    /** Identificador único del privilegio, generado por Oracle. */
    private Long id;

    /**
     * Código único del privilegio. Convenio: {@code RECURSO_ACCION}.
     * Ejemplos: USR_CREAR, USR_LEER, USR_EDITAR, USR_ELIMINAR, GRP_ADMIN.
     */
    private String codigo;

    /** Nombre legible del privilegio (ej: "Crear Usuario"). */
    private String nombre;

    /** Descripción del permiso que otorga (ej: "Permite crear nuevos usuarios"). */
    private String descripcion;

    /**
     * Verbo HTTP que habilita este privilegio: GET, POST, PUT, PATCH, DELETE, ALL.
     * Informativo; la autorización real la gestiona Spring Security.
     */
    private String accion;

    /**
     * Ruta del recurso protegido por este privilegio.
     * Ejemplo: {@code /api/v1/usuarios}.
     */
    private String recurso;

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
