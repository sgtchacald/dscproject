package br.com.diegocordeiro.dscproject.repository.fatura;

import br.com.diegocordeiro.dscproject.model.fatura.FaturaCartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaCartaoRepository extends JpaRepository<FaturaCartao, Long> {

    Optional<FaturaCartao> findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    Optional<FaturaCartao> findByCartaoIdAndCompetenciaAndDataExclusaoIsNull(Long cartaoId, YearMonth competencia);

    boolean existsByCartaoIdAndCompetenciaAndDataExclusaoIsNull(Long cartaoId, YearMonth competencia);

    @Query("""
        SELECT f FROM FaturaCartao f
        JOIN FETCH f.cartao c
        LEFT JOIN FETCH f.transacaoPagamento tp
        WHERE c.id = :cartaoId
          AND c.usuario.id = :usuarioId
          AND f.dataExclusao IS NULL
        ORDER BY f.competencia ASC
        """)
    List<FaturaCartao> listarPorCartaoEUsuario(@Param("cartaoId") Long cartaoId, @Param("usuarioId") Long usuarioId);
}
