package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConsentimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpenFinanceConsentimentoRepository extends JpaRepository<OpenFinanceConsentimento, Long> {

    Optional<OpenFinanceConsentimento> findByConexaoIdAndDataExclusaoIsNull(Long conexaoId);

    Optional<OpenFinanceConsentimento> findByConexaoId(Long conexaoId);

    Optional<OpenFinanceConsentimento> findByIdExterno(String idExterno);
}
