package usuarios.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para crear o actualizar un grupo (rol).
 *
 * <p>Usado en los endpoints:</p>
 * <ul>
 *   <li>{@code POST /api/v1/grupos}    – creación.</li>
 *   <li>{@code PUT  /api/v1/grupos/{id}} – actualización de nombre y descripción.</li>
 * </ul>
 *
 * <p>Ejemplo de JSON de entrada:</p>
 * <pre>
 * {
 *   "nombre":      "PRODUCTOR",
 *   "descripcion": "Productor agrícola que gestiona lugares y lotes"
 * }
 * </pre>
 */
public class GrupoRequest {

    /**
     * Nombre único del grupo. Actúa como identificador semántico del rol.
     * Convención: mayúsculas y sin espacios (ej: ADMINISTRADOR, ASISTENTE_TECNICO).
     * El servicio valida unicidad antes de persistir.
     */
    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String nombre;

    /**
     * Descripción del propósito del grupo. Campo opcional.
     * Ejemplo: "Gestiona inspecciones fitosanitarias en campo".
     */
    private String descripcion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
