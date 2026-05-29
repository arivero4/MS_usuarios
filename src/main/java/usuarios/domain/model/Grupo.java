package usuarios.domain.model;

import usuarios.domain.enums.Estado;

import java.util.ArrayList;
import java.util.List;

public class Grupo {

    private Long id;
    private String nombre;
    private String descripcion;
    private Estado estado;
    private List<Privilegio> privilegios;

    public Grupo() {
        this.privilegios = new ArrayList<>();
    }

    public Grupo(Long id, String nombre, String descripcion, Estado estado, List<Privilegio> privilegios) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.privilegios = privilegios != null ? privilegios : new ArrayList<>();
    }

    public boolean estaActivo() {
        return Estado.ACTIVO.equals(this.estado);
    }

    public void activar() { this.estado = Estado.ACTIVO; }
    public void desactivar() { this.estado = Estado.INACTIVO; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public List<Privilegio> getPrivilegios() { return privilegios; }
    public void setPrivilegios(List<Privilegio> privilegios) { this.privilegios = privilegios; }
}
