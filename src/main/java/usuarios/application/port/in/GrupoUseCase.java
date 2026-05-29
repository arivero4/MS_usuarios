package usuarios.application.port.in;

import usuarios.domain.model.Grupo;

import java.util.List;

public interface GrupoUseCase {

    Grupo crearGrupo(Grupo grupo);

    Grupo obtenerGrupoPorId(Long id);

    List<Grupo> listarGrupos();

    Grupo actualizarGrupo(Long id, Grupo grupo);

    void eliminarGrupo(Long id);

    Grupo asignarPrivilegio(Long grupoId, Long privilegioId);

    Grupo removerPrivilegio(Long grupoId, Long privilegioId);
}
