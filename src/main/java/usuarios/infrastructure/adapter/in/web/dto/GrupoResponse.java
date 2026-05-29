package usuarios.infrastructure.adapter.in.web.dto;

import java.util.List;

public class GrupoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String estado;
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
