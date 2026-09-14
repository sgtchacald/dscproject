package br.com.diegocordeiro.dscproject.repository.parametro;

import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParametroGlobalRepository
        extends JpaRepository<ParametroGlobal, Long>, RevisionRepository<ParametroGlobal, Long, Integer> {

    /** Listagem do grid: parâmetros ativos, agrupados por módulo e depois nome. */
    List<ParametroGlobal> findByDataExclusaoIsNullOrderByModuloAscNomeAsc();

    /** Chave estável — leitura por código, sincronizador e runner de inicialização. */
    Optional<ParametroGlobal> findByCodigo(String codigo);

    /** Estado atual de um parâmetro ativo, antes de confirmar uma alteração inline. */
    Optional<ParametroGlobal> findByIdAndDataExclusaoIsNull(Long id);
}
