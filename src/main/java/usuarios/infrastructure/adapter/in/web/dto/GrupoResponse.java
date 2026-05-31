package usuarios.infrastructure.adapter.in.web.dto;

import java.util.List;

/**
 * DTO de salida que representa un grupo en las respuestas REST.
 *
 * <p>Aparece como campo anidado dentro de {@link UsuarioResponse#getGrupos()}
 * y también como respuesta directa de los endpoints {@code /api/v1/grupos}.</p>
 *
 * <p>Ejemplo de JSON de salida:</p>
 * <pre>
 * {
 *   "id":          1,
 *   "nombre":      "ADMINISTRADOR",
 *   "descripcion": "Grupo con acceso total al sistema",
 *   "estado":      "ACTIVO",
 *   "privilegios": [
 *     { "id": 1, "codigo": "USR_CREAR", "nombre": "Crear Usuario", ... }
 *   ]
 * }
 * </pre>
 */
public class GrupoResponse {

    /** Identificador único del grupo, generado por Oracle. */
    private Long id;

    /** Nombre único del grupo / rol (ej: "ADMINISTRADOR", "PRODUCTOR"). */
    private String nombre;

    /** Descripción del propósito del grupo. Puede ser null. */
    private String descripcion;

    /**
     * Estado del grupo como String (ACTIVO o INACTIVO).
     * Un grupo inactivo no puede asignarse a nuevos usuarios.
     */
    private String estado;

    /**
     * Lista de privilegios asignados a este grupo.
     * Define las acciones concretas que pueden realizar los miembros del grupo.
     * Lista vacía si el grupo no tiene privilegios asignados.
     */
    private List<PrivilegioResponse> privilegios;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<PrivilegioResponse> getPrivilegios() { return privilegios; }
    public void setPrivilegios(List<PrivilegioResponse> privilegios) { this.privilegios = privilegios; }
}
