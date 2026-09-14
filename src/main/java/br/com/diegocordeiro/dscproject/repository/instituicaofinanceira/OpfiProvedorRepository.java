package br.com.diegocordeiro.dscproject.repository.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpfiProvedorRepository extends JpaRepository<OpfiProvedor, Long> {

    Optional<OpfiProvedor> findByCodigo(String codigo);

    List<OpfiProvedor> findByDataExclusaoIsNullOrderByNomeAsc();

    List<OpfiProvedor> findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc();
}
