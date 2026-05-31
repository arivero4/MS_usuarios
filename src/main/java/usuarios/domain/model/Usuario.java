package usuarios.domain.model;

import usuarios.domain.enums.Estado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa a un usuario del sistema TerraIca.
 *
 * <p>Pertenece a la capa de dominio (núcleo hexagonal) y no depende de ningún
 * framework externo. Encapsula las reglas de negocio del ciclo de vida
 * del usuario: activación, suspensión y bloqueo.</p>
 *
 * <p>Se persiste en la tabla {@code USUARIO} via
 * {@link usuarios.infrastructure.adapter.out.persistence.entity.UsuarioEntity}.</p>
 */
public class Usuario {

    /** Identificador único, generado por la secuencia {@code SEQ_USUARIO} de Oracle. */
    private Long id;

    /** Cédula o NIT. Único en el sistema (constraint {@code UK_USUARIO_NUM_ID}). */
    private String numeroIdentificacion;

    /** Nombre completo del usuario. */
    private String nombre;

    /**
     * Correo electrónico. Actúa como nombre de usuario para el login y
     * como {@code subject} del JWT emitido por
     * {@link usuarios.infrastructure.adapter.out.security.JwtTokenAdapter}.
     * Único en el sistema (constraint {@code UK_USUARIO_CORREO}).
     */
    private String correo;

    /** Teléfono de contacto. Campo opcional. */
    private String telefono;

    /**
     * Contraseña almacenada siempre cifrada con BCrypt.
     * Nunca se expone en las respuestas REST.
     * El cifrado lo realiza
     * {@link usuarios.infrastructure.adapter.out.encoder.BcryptPasswordEncoderAdapter}.
     */
    private String password;

    /**
     * Número de tarjeta profesional del ICA.
     * Usado principalmente por Asistentes Técnicos Fitosanitarios. Opcional.
     */
    private String numeroTarjetaProfesional;

    /**
     * Estado actual del ciclo de vida del usuario.
     * Ver {@link Estado} para los valores posibles y sus transiciones.
     */
    private Estado estado;

    /** Timestamp de creación. Asignado en {@link usuarios.application.service.UsuarioService#crearUsuario}. */
    private LocalDateTime fechaCreacion;

    /** Timestamp de la última modificación. Actualizado en cada operación de escritura. */
    private LocalDateTime fechaActualizacion;

    /**
     * Grupos (roles) asignados al usuario. Relación N:M con la tabla {@code GRUPOS_USUARIO}.
     * Los nombres de los grupos se incluyen como claim {@code "grupos"} en el JWT,
     * permitiendo que los demás microservicios autoricen peticiones sin consultar la BD.
     */
    private List<Grupo> grupos;

    /** Constructor vacío. Inicializa {@code grupos} para evitar NullPointerException. */
    public Usuario() {
        this.grupos = new ArrayList<>();
    }

    /**
     * Constructor completo. Utilizado por
     * {@link usuarios.infrastructure.adapter.out.persistence.mapper.PersistenceMapper#toDomain}
     * al reconstruir el objeto desde la base de datos.
     */
    public Usuario(Long id, String numeroIdentificacion, String nombre, String correo,
                   String telefono, String password, String numeroTarjetaProfesional,
                   Estado estado, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
                   List<Grupo> grupos) {
        this.id = id;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.password = password;
        this.numeroTarjetaProfesional = numeroTarjetaProfesional;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.grupos = grupos != null ? grupos : new ArrayList<>();
    }

    // ── Métodos de negocio ────────────────────────────────────────────────────

    /**
     * Verifica si el usuario puede operar en el sistema.
     * Consultado en {@link usuarios.application.service.AuthService#autenticar}
     * para bloquear el login de usuarios no activos.
     *
     * @return {@code true} si el estado es {@link Estado#ACTIVO}.
     */
    public boolean estaActivo() {
        return Estado.ACTIVO.equals(this.estado);
    }

    /**
     * Verifica si el usuario está bloqueado permanentemente.
     * Un usuario bloqueado recibe error {@code AUTH-002} al intentar autenticarse.
     *
     * @return {@code true} si el estado es {@link Estado#BLOQUEADO}.
     */
    public boolean estaBloqueado() {
        return Estado.BLOQUEADO.equals(this.estado);
    }

    /**
     * Transición de estado → {@link Estado#ACTIVO}.
     * Actualiza {@code fechaActualizacion} al instante de la llamada.
     * Invocado desde {@link usuarios.application.service.UsuarioService#cambiarEstado}.
     */
    public void activar() {
        this.estado = Estado.ACTIVO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Transición de estado → {@link Estado#BLOQUEADO}.
     * Actualiza {@code fechaActualizacion}.
     */
    public void bloquear() {
        this.estado = Estado.BLOQUEADO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Transición de estado → {@link Estado#SUSPENDIDO} (bloqueo temporal).
     * Actualiza {@code fechaActualizacion}.
     */
    public void suspender() {
        this.estado = Estado.SUSPENDIDO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

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

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<Grupo> getGrupos() { return grupos; }
    public void setGrupos(List<Grupo> grupos) { this.grupos = grupos; }
}
