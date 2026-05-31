package usuarios.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear o actualizar un usuario.
 *
 * <p>Usado en los endpoints:</p>
 * <ul>
 *   <li>{@code POST /api/v1/usuarios}    – creación (todos los campos con @NotBlank son requeridos).</li>
 *   <li>{@code PUT  /api/v1/usuarios/{id}} – actualización (solo nombre, teléfono y tarjeta se modifican).</li>
 * </ul>
 *
 * <p>Ejemplo de JSON de entrada para crear:</p>
 * <pre>
 * {
 *   "numeroIdentificacion": "1234567890",
 *   "nombre":               "Juan Pérez",
 *   "correo":               "juan@empresa.com",
 *   "telefono":             "3001234567",
 *   "password":             "MiClave2024!",
 *   "numeroTarjetaProfesional": "TP-12345"
 * }
 * </pre>
 */
public class UsuarioRequest {

    /**
     * Cédula o NIT del usuario. Debe ser único en el sistema
     * (validado por el servicio, no por esta anotación).
     */
    @NotBlank(message = "El número de identificación es obligatorio")
    private String numeroIdentificacion;

    /** Nombre completo del usuario. Requerido en creación y actualización. */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    /**
     * Correo electrónico único. Actúa como identificador de login.
     * Validaciones:
     * <ul>
     *   <li>{@code @NotBlank} – no vacío.</li>
     *   <li>{@code @Email}    – formato válido de correo.</li>
     * </ul>
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    /**
     * Teléfono de contacto. Campo opcional (sin {@code @NotBlank}).
     * No tiene formato validado para soportar distintos formatos internacionales.
     */
    private String telefono;

    /**
     * Contraseña en texto plano. Se cifra con BCrypt antes de persistirse.
     * Validaciones:
     * <ul>
     *   <li>{@code @NotBlank} – no vacío.</li>
     *   <li>{@code @Size(min=8)} – mínimo 8 caracteres por política de seguridad.</li>
     * </ul>
     * En actualizaciones ({@code PUT}), la contraseña no se modifica aunque
     * se incluya en el request (el servicio ignora este campo en actualizar).
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    /**
     * Número de tarjeta profesional del ICA. Campo opcional.
     * Usado principalmente por Asistentes Técnicos Fitosanitarios.
     */
    private String numeroTarjetaProfesional;

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
}
