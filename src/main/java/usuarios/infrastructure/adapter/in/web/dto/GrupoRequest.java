package usuarios.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public class GrupoRequest {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String nombre;

    private String descripcion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
