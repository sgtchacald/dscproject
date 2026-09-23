package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceSincronizacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceSincronizacaoRepository extends JpaRepository<OpenFinanceSincronizacao, Long> {

    Page<OpenFinanceSincronizacao> findByConexaoIdOrderByIniciadoEmDesc(Long conexaoId, Pageable pageable);

    List<OpenFinanceSincronizacao> findByConexaoIdOrderByIniciadoEmDesc(Long conexaoId);

    Optional<OpenFinanceSincronizacao> findTopByConexaoIdOrderByIniciadoEmDesc(Long conexaoId);
}
