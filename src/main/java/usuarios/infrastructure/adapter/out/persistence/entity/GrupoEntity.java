package usuarios.infrastructure.adapter.out.persistence.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de persistencia que representa una fila de la tabla {@code GRUPOS} en Oracle.
 *
 * <p>Columnas de la tabla {@code GRUPOS}:</p>
 * <pre>
 *  ID          NUMBER(19)     PK (SEQ_GRUPOS)
 *  NOMBRE      VARCHAR2(100)  NOT NULL, UNIQUE (UK_GRUPOS_NOMBRE)
 *  DESCRIPCION VARCHAR2(500)  NULL
 *  ESTADO      VARCHAR2(20)   NOT NULL CHECK(ACTIVO|INACTIVO|SUSPENDIDO|BLOQUEADO)
 *  FECHA_CREACION DATE        NULL (agregada en migración)
 * </pre>
 */
public class GrupoEntity {

    private Long id;
    private String nombre;
    private String descripcion;
    /** Estado como String: ACTIVO, INACTIVO, SUSPENDIDO o BLOQUEADO. */
    private String estado;

    /**
     * Privilegios asignados al grupo. No mapea a una columna directa;
     * se carga mediante JOIN con {@code PRIVILEGIO_GRUPO} y {@code PRIVILEGIOS}.
     */
    private List<PrivilegioEntity> privilegios;

    /** Constructor vacío. Inicializa {@code privilegios} para evitar NullPointerException. */
    public GrupoEntity() {
        this.privilegios = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<PrivilegioEntity> getPrivilegios() { return privilegios; }
    public void setPrivilegios(List<PrivilegioEntity> privilegios) { this.privilegios = privilegios; }
}
