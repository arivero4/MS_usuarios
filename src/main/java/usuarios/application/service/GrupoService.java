package usuarios.application.service;

import org.springframework.stereotype.Service;
import usuarios.application.port.in.GrupoUseCase;
import usuarios.application.port.out.GrupoRepositoryPort;
import usuarios.application.port.out.PrivilegioRepositoryPort;
import usuarios.domain.exception.GrupoNotFoundException;
import usuarios.domain.exception.UsuarioException;
import usuarios.domain.enums.Estado;
import usuarios.domain.model.Grupo;
import usuarios.domain.model.Privilegio;

import java.util.List;

@Service
public class GrupoService implements GrupoUseCase {

    private final GrupoRepositoryPort grupoRepository;
    private final PrivilegioRepositoryPort privilegioRepository;

    public GrupoService(GrupoRepositoryPort grupoRepository,
                        PrivilegioRepositoryPort privilegioRepository) {
        this.grupoRepository     = grupoRepository;
        this.privilegioRepository = privilegioRepository;
    }

    @Override
    public Grupo crearGrupo(Grupo grupo) {
        if (grupoRepository.existePorNombre(grupo.getNombre())) {
            throw new UsuarioException("GRP-002", "Ya existe un grupo con el nombre: " + grupo.getNombre());
        }
        grupo.setEstado(Estado.ACTIVO);
        return grupoRepository.guardar(grupo);
    }

    @Override
    public Grupo obtenerGrupoPorId(Long id) {
        return grupoRepository.buscarPorId(id)
                .orElseThrow(() -> new GrupoNotFoundException(id));
    }

    @Override
    public List<Grupo> listarGrupos() {
        return grupoRepository.buscarTodos();
    }

    @Override
    public Grupo actualizarGrupo(Long id, Grupo datos) {
        Grupo existente = obtenerGrupoPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        return grupoRepository.actualizar(existente);
    }

    @Override
    public void eliminarGrupo(Long id) {
        obtenerGrupoPorId(id);
        grupoRepository.eliminar(id);
    }

    @Override
    public Grupo asignarPrivilegio(Long grupoId, Long privilegioId) {
        Grupo grupo = obtenerGrupoPorId(grupoId);
        Privilegio privilegio = privilegioRepository.buscarPorId(privilegioId)
                .orElseThrow(() -> new UsuarioException("PRV-001", "Privilegio no encontrado: " + privilegioId));
        boolean yaAsignado = grupo.getPrivilegios().stream()
                .anyMatch(p -> p.getId().equals(privilegioId));
        if (!yaAsignado) {
            grupo.getPrivilegios().add(privilegio);
            return grupoRepository.actualizar(grupo);
        }
        return grupo;
    }

    @Override
    public Grupo removerPrivilegio(Long grupoId, Long privilegioId) {
        Grupo grupo = obtenerGrupoPorId(grupoId);
        grupo.getPrivilegios().removeIf(p -> p.getId().equals(privilegioId));
        return grupoRepository.actualizar(grupo);
    }
}
