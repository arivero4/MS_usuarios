package usuarios.application.port.out;

import usuarios.domain.model.Usuario;

public interface TokenPort {

    String generarToken(Usuario usuario);

    boolean validarToken(String token);

    String extraerCorreo(String token);

    String renovarToken(String token);
}
