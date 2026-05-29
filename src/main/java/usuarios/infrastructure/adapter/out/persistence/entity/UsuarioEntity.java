package usuarios.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa la tabla USUARIOS en Oracle 10g.
 * Se usa JDBC puro (sin JPA) por compatibilidad con Oracle 10g.
 */
public class UsuarioEntity {

    private Long id;
    private String numeroIdentificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private String password;
    private String numeroTarjetaProfesional;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<GrupoEntity> grupos;

    public UsuarioEntity() {
        this.grupos = new ArrayList<>();
    }

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<GrupoEntity> getGrupos() { return grupos; }
    public void setGrupos(List<GrupoEntity> grupos) { this.grupos = grupos; }
}
