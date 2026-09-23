package br.com.diegocordeiro.dscproject.repository.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceProvedorRepository extends JpaRepository<OpenFinanceProvedor, Long> {

    Optional<OpenFinanceProvedor> findByCodigo(String codigo);

    List<OpenFinanceProvedor> findByDataExclusaoIsNullOrderByNomeAsc();

    List<OpenFinanceProvedor> findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc();
}
