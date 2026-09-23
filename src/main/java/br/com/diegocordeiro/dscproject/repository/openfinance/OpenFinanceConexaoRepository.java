package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceConexaoRepository extends JpaRepository<OpenFinanceConexao, Long> {

    List<OpenFinanceConexao> findByUsuarioIdAndDataExclusaoIsNull(Long usuarioId);

    Optional<OpenFinanceConexao> findByIdAndUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    Optional<OpenFinanceConexao> findByIdExternoAndProvedorIdAndDataExclusaoIsNull(String idExterno, Long provedorId);

    Optional<OpenFinanceConexao> findByIdExternoAndDataExclusaoIsNull(String idExterno);

    Optional<OpenFinanceConexao> findByIdExterno(String idExterno);
}
