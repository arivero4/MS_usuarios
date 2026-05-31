package usuarios.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida que representa un usuario en las respuestas REST.
 *
 * <p><strong>Seguridad:</strong> la contraseña ({@code password}) no se incluye
 * para evitar exponerla al cliente, aunque esté cifrada con BCrypt.</p>
 *
 * <p>Ejemplo de JSON de salida:</p>
 * <pre>
 * {
 *   "id":                    1,
 *   "numeroIdentificacion":  "1234567890",
 *   "nombre":                "Juan Pérez",
 *   "correo":                "juan@empresa.com",
 *   "telefono":              "3001234567",
 *   "numeroTarjetaProfesional": null,
 *   "estado":                "ACTIVO",
 *   "fechaCreacion":         "2026-01-15 10:30:00",
 *   "fechaActualizacion":    "2026-01-15 10:30:00",
 *   "grupos": [
 *     {
 *       "id": 1,
 *       "nombre": "ADMINISTRADOR",
 *       "estado": "ACTIVO",
 *       "privilegios": [...]
 *     }
 *   ]
 * }
 * </pre>
 */
public class UsuarioResponse {

    /** Identificador único generado por Oracle. */
    private Long id;

    /** Cédula o NIT del usuario. */
    private String numeroIdentificacion;

    /** Nombre completo del usuario. */
    private String nombre;

    /** Correo electrónico (identificador de login). */
    private String correo;

    /** Teléfono de contacto. Puede ser null. */
    private String telefono;

    /** Tarjeta profesional del ICA. Puede ser null. */
    private String numeroTarjetaProfesional;

    /**
     * Estado actual del usuario como String (ACTIVO, INACTIVO, SUSPENDIDO, BLOQUEADO).
     * Se serializa como String en lugar del enum para mayor compatibilidad con clientes.
     */
    private String estado;

    /** Timestamp de creación. Serializado como "yyyy-MM-dd HH:mm:ss" por la config de Jackson. */
    private LocalDateTime fechaCreacion;

    /** Timestamp de última modificación. */
    private LocalDateTime fechaActualizacion;

    /**
     * Grupos (roles) asignados al usuario, incluyendo sus privilegios.
     * Lista vacía si el usuario no tiene grupos asignados.
     */
    private List<GrupoResponse> grupos;

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

    public String getNumeroTarjetaProfesional() { return numeroTarjetaProfesional; }
    public void setNumeroTarjetaProfesional(String n) { this.numeroTarjetaProfesional = n; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<GrupoResponse> getGrupos() { return grupos; }
    public void setGrupos(List<GrupoResponse> grupos) { this.grupos = grupos; }
}
