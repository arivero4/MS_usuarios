package usuarios.application.service;

import org.springframework.stereotype.Service;
import usuarios.application.port.in.AuthUseCase;
import usuarios.application.port.out.PasswordEncoderPort;
import usuarios.application.port.out.TokenPort;
import usuarios.application.port.out.UsuarioRepositoryPort;
import usuarios.domain.exception.CredencialesInvalidasException;
import usuarios.domain.exception.UsuarioException;
import usuarios.domain.model.Usuario;

@Service
public class AuthService implements AuthUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenPort tokenPort;
    private final PasswordEncoderPort passwordEncoder;

    public AuthService(UsuarioRepositoryPort usuarioRepository,
                       TokenPort tokenPort,
                       PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenPort         = tokenPort;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    public String autenticar(String correo, String password) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        if (usuario.estaBloqueado()) {
            throw new UsuarioException("AUTH-002", "Usuario bloqueado. Contacte al administrador.");
        }
        if (!usuario.estaActivo()) {
            throw new UsuarioException("AUTH-003", "Usuario inactivo.");
        }
        return tokenPort.generarToken(usuario);
    }

    @Override
    public boolean validarToken(String token) {
        return tokenPort.validarToken(token);
    }

    @Override
    public String renovarToken(String token) {
        if (!tokenPort.validarToken(token)) {
            throw new UsuarioException("AUTH-004", "Token inválido o expirado.");
        }
        return tokenPort.renovarToken(token);
    }
}
