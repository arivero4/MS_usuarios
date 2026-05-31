package usuarios.domain.model;

import usuarios.domain.enums.Estado;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa un grupo (rol) del sistema.
 *
 * <p>Un grupo agrupa a varios {@link Usuario}s y contiene una lista de
 * {@link Privilegio}s que definen qué acciones pueden ejecutar sus miembros.
 * Ejemplos de grupos del sistema: ADMINISTRADOR, PROPIETARIO, PRODUCTOR,
 * ASISTENTE_TECNICO.</p>
 *
 * <p>Se persiste en la tabla {@code GRUPOS}. Las relaciones N:M se manejan
 * en {@code GRUPOS_USUARIO} (usuarios asignados) y
 * {@code PRIVILEGIO_GRUPO} (privilegios asignados).</p>
 *
 * <p>El nombre del grupo se incluye como claim {@code "grupos"} en el JWT,
 * lo que permite a los microservicios del ecosistema autorizar peticiones
 * sin necesidad de consultar la base de datos de usuarios.</p>
 */
public class Grupo {

    /** Identificador único, generado por la secuencia {@code SEQ_GRUPOS} de Oracle. */
    private Long id;

    /** Nombre único del grupo (constraint {@code UK_GRUPOS_NOMBRE}). Es el valor
     * que se incluye en el claim JWT y debe coincidir con los roles esperados
     * por los demás microservicios (p. ej. {@code ROLE_ADMINISTRADOR}). */
    private String nombre;

    /** Descripción del propósito del grupo. Campo opcional. */
    private String descripcion;

    /** Estado del grupo. Permite activar o desactivar un rol completo
     * sin necesidad de modificar individualmente a cada usuario. */
    private Estado estado;

    /**
     * Privilegios asignados al grupo. Relación N:M con la tabla {@code PRIVILEGIO_GRUPO}.
     * Define las acciones concretas que pueden realizar los usuarios del grupo.
     */
    private List<Privilegio> privilegios;

    /** Constructor vacío. Inicializa {@code privilegios} para evitar NullPointerException. */
    public Grupo() {
        this.privilegios = new ArrayList<>();
    }

    /**
     * Constructor completo. Utilizado por
     * {@link usuarios.infrastructure.adapter.out.persistence.mapper.PersistenceMapper#toDomain(usuarios.infrastructure.adapter.out.persistence.entity.GrupoEntity)}.
     */
    public Grupo(Long id, String nombre, String descripcion, Estado estado, List<Privilegio> privilegios) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.privilegios = privilegios != null ? privilegios : new ArrayList<>();
    }

    // ── Métodos de negocio ────────────────────────────────────────────────────

    /**
     * Verifica si el grupo está operativo.
     * Un grupo inactivo no puede asignarse a nuevos usuarios.
     *
     * @return {@code true} si el estado es {@link Estado#ACTIVO}.
     */
    public boolean estaActivo() {
        return Estado.ACTIVO.equals(this.estado);
    }

    /**
     * Transición de estado → {@link Estado#ACTIVO}.
     * Invocado desde {@link usuarios.application.service.GrupoService}.
     */
    public void activar() { this.estado = Estado.ACTIVO; }

    /**
     * Transición de estado → {@link Estado#INACTIVO}.
     * Desactiva el grupo sin eliminar su historial ni sus miembros.
     */
    public void desactivar() { this.estado = Estado.INACTIVO; }

    // ── Getters y Setters ─────────────────────────────────────────────────────

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
