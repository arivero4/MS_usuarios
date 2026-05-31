package usuarios.application.port.out;

import usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (driven port) que abstrae la persistencia de usuarios.
 *
 * <p>Define el contrato que el dominio y la aplicación requieren para
 * almacenar y recuperar usuarios, sin conocer ningún detalle de implementación
 * (Oracle, SQL, JDBC). Esto permite cambiar la BD sin tocar la lógica de negocio.</p>
 *
 * <p>Implementado por:
 * {@link usuarios.infrastructure.adapter.out.persistence.repository.UsuarioRepositoryAdapter}
 * usando {@code JdbcTemplate} contra Oracle 10g.</p>
 */
public interface UsuarioRepositoryPort {

    /**
     * Persiste un nuevo usuario en la base de datos.
     * La secuencia {@code SEQ_USUARIO} de Oracle asigna el ID automáticamente.
     *
     * @param usuario Objeto de dominio con todos los datos (contraseña ya cifrada).
     * @return El mismo usuario con el ID generado por Oracle.
     */
    Usuario guardar(Usuario usuario);

    /**
     * Busca un usuario por su ID numérico.
     * Carga también los grupos y privilegios asociados.
     *
     * @param id ID del usuario.
     * @return {@link Optional} con el usuario si existe, vacío si no.
     */
    Optional<Usuario> buscarPorId(Long id);

    /**
     * Busca un usuario por su correo electrónico.
     * Usado principalmente en {@link usuarios.application.service.AuthService#autenticar}
     * y en {@link usuarios.infrastructure.config.JwtAuthFilter}.
     *
     * @param correo Correo electrónico único del usuario.
     * @return {@link Optional} con el usuario si existe, vacío si no.
     */
    Optional<Usuario> buscarPorCorreo(String correo);

    /**
     * Busca un usuario por su número de identificación (cédula/NIT).
     *
     * @param numeroIdentificacion Número de identificación único.
     * @return {@link Optional} con el usuario si existe, vacío si no.
     */
    Optional<Usuario> buscarPorNumeroIdentificacion(String numeroIdentificacion);

    /**
     * Recupera todos los usuarios del sistema con sus grupos.
     *
     * @return Lista de usuarios. Vacía si no hay ninguno, nunca null.
     */
    List<Usuario> buscarTodos();

    /**
     * Actualiza los datos de un usuario existente en la BD.
     * También sincroniza la tabla {@code GRUPOS_USUARIO} con los grupos actuales del dominio.
     *
     * @param usuario Objeto de dominio con los datos actualizados (debe tener ID).
     * @return El usuario con los datos actualizados.
     */
    Usuario actualizar(Usuario usuario);

    /**
     * Elimina un usuario de la BD. Primero elimina sus registros en
     * {@code GRUPOS_USUARIO} para respetar las claves foráneas.
     *
     * @param id ID del usuario a eliminar.
     */
    void eliminar(Long id);

    /**
     * Verifica si ya existe un usuario con ese correo (para validar unicidad antes de crear).
     *
     * @param correo Correo a verificar.
     * @return {@code true} si el correo ya está registrado.
     */
    boolean existePorCorreo(String correo);

    /**
     * Verifica si ya existe un usuario con ese número de identificación.
     *
     * @param numeroIdentificacion Número a verificar.
     * @return {@code true} si ya está registrado.
     */
    boolean existePorNumeroIdentificacion(String numeroIdentificacion);
}
