package usuarios.domain.exception;

public class GrupoNotFoundException extends UsuarioException {

    public GrupoNotFoundException(Long id) {
        super("GRP-001", "Grupo no encontrado con id: " + id);
    }
}
