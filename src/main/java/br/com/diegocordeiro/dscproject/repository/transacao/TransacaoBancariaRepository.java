package br.com.diegocordeiro.dscproject.repository.transacao;

import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoBancariaRepository extends JpaRepository<TransacaoBancaria, Long> {

    Optional<TransacaoBancaria> findByIdAndContaUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    Optional<TransacaoBancaria> findByIdAndContaUsuarioId(Long id, Long usuarioId);

    boolean existsByIdExternoAndContaIdAndDataExclusaoIsNull(String idExterno, Long contaId);

    @Query("""
        SELECT t FROM TransacaoBancaria t
        JOIN FETCH t.conta c
        LEFT JOIN FETCH t.categoria cat
        WHERE c.usuario.id = :usuarioId
          AND (:busca IS NULL OR LOWER(t.descricao) LIKE LOWER(CONCAT('%', :busca, '%')))
          AND (:competenciaInicio IS NULL OR t.competencia >= :competenciaInicio)
          AND (:competenciaFim IS NULL OR t.competencia <= :competenciaFim)
          AND (:contaId IS NULL OR c.id = :contaId)
          AND (:natureza IS NULL OR t.natureza = :natureza)
          AND (:categoriaId IS NULL OR (cat IS NOT NULL AND cat.id = :categoriaId))
        ORDER BY t.competencia DESC, t.dataLancamento DESC, t.id DESC
        """)
    List<TransacaoBancaria> listarComFiltros(
        @Param("usuarioId") Long usuarioId,
        @Param("busca") String busca,
        @Param("competenciaInicio") YearMonth competenciaInicio,
        @Param("competenciaFim") YearMonth competenciaFim,
        @Param("contaId") Long contaId,
        @Param("natureza") NaturezaMovimento natureza,
        @Param("categoriaId") Long categoriaId
    );

    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) FROM TransacaoBancaria t
        JOIN t.conta c
        WHERE c.usuario.id = :usuarioId
          AND t.competencia = :competencia
          AND t.natureza = :natureza
          AND t.dataExclusao IS NULL
        """)
    BigDecimal somarPorCompetenciaENaturezaEUsuario(@Param("competencia") YearMonth competencia, @Param("natureza") NaturezaMovimento natureza, @Param("usuarioId") Long usuarioId);
}
