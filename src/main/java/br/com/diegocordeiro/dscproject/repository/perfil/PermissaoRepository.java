package br.com.diegocordeiro.dscproject.repository.perfil;

import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    Optional<Permissao> findByCodigo(String codigo);

    /** Catálogo para a tela e o seletor de perfil, agrupável por módulo. */
    List<Permissao> findByDataExclusaoIsNullOrderByModuloAscNomeAsc();
}
