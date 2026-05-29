package usuarios.domain.exception;

public class UsuarioException extends RuntimeException {

    private final String codigo;

    public UsuarioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public UsuarioException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
