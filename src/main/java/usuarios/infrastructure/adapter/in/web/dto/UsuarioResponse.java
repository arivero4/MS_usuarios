package usuarios.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioResponse {

    private Long id;
    private String numeroIdentificacion;
    private String nombre;
    private String correo;
    private String telefono;
    private String numeroTarjetaProfesional;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
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
