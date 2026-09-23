package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceContaExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceContaExternaRepository extends JpaRepository<OpenFinanceContaExterna, Long> {

    List<OpenFinanceContaExterna> findByConexaoIdAndDataExclusaoIsNull(Long conexaoId);

    List<OpenFinanceContaExterna> findByConexaoUsuarioIdAndDataExclusaoIsNull(Long usuarioId);

    Optional<OpenFinanceContaExterna> findByIdExterno(String idExterno);

    Optional<OpenFinanceContaExterna> findByIdAndConexaoUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    boolean existsByContaIdAndDataExclusaoIsNull(Long contaId);

    boolean existsByCartaoCreditoIdAndDataExclusaoIsNull(Long cartaoCreditoId);
}
