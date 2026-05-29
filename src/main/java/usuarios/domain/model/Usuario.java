package usuarios.domain.model;

import usuarios.domain.enums.Estado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private Long id;
    private String numeroIdentificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private String password;
    private String numeroTarjetaProfesional;
    private Estado estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<Grupo> grupos;

    public Usuario() {
        this.grupos = new ArrayList<>();
    }

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

    public boolean estaActivo() {
        return Estado.ACTIVO.equals(this.estado);
    }

    public boolean estaBloqueado() {
        return Estado.BLOQUEADO.equals(this.estado);
    }

    public void activar() {
        this.estado = Estado.ACTIVO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void bloquear() {
        this.estado = Estado.BLOQUEADO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void suspender() {
        this.estado = Estado.SUSPENDIDO;
        this.fechaActualizacion = LocalDateTime.now();
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

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<Grupo> getGrupos() { return grupos; }
    public void setGrupos(List<Grupo> grupos) { this.grupos = grupos; }
}
