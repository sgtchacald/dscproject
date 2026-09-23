package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceFaturaRepository extends JpaRepository<OpenFinanceFatura, Long> {

    List<OpenFinanceFatura> findByContaExternaId(Long contaExternaId);

    Optional<OpenFinanceFatura> findByIdExterno(String idExterno);

    boolean existsByIdExterno(String idExterno);
}
