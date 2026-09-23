package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceTransacaoRepository extends JpaRepository<OpenFinanceTransacao, Long> {

    List<OpenFinanceTransacao> findByContaExternaId(Long contaExternaId);

    Optional<OpenFinanceTransacao> findByIdExterno(String idExterno);

    boolean existsByIdExterno(String idExterno);
}
