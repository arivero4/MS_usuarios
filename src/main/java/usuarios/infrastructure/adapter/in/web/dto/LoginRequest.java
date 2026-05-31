package usuarios.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el endpoint {@code POST /api/v1/auth/login}.
 *
 * <p>Contiene las credenciales del usuario. Las anotaciones de validación
 * de Jakarta ({@code @NotBlank}, {@code @Email}) son evaluadas por Spring
 * cuando el controlador usa {@code @Valid @RequestBody LoginRequest}.
 * Si alguna falla, Spring lanza {@code MethodArgumentNotValidException}
 * que el {@link usuarios.infrastructure.adapter.in.web.GlobalExceptionHandler}
 * traduce a {@code 400 Bad Request}.</p>
 *
 * <p>Ejemplo de JSON de entrada:</p>
 * <pre>
 * {
 *   "correo":   "admin@empresa.com",
 *   "password": "Admin1234!"
 * }
 * </pre>
 */
public class LoginRequest {

    /**
     * Correo electrónico del usuario. Actúa como nombre de usuario.
     * Validaciones aplicadas:
     * <ul>
     *   <li>{@code @NotBlank} – no puede ser null, vacío ni solo espacios.</li>
     *   <li>{@code @Email}    – debe tener formato de correo válido (contiene "@" y dominio).</li>
     * </ul>
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    /**
     * Contraseña en texto plano ingresada por el usuario.
     * Solo se valida que no esté vacía; la complejidad se validó al crear el usuario.
     * Nunca se almacena ni se loguea en texto plano.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
