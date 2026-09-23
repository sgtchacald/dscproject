package br.com.diegocordeiro.dscproject.repository.despesa;

import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @Query("""
        SELECT DISTINCT d FROM Despesa d
        LEFT JOIN FETCH d.conta c
        LEFT JOIN FETCH d.cartao cc
        LEFT JOIN FETCH d.categoria cat
        LEFT JOIN FETCH d.rateios r
        WHERE d.id = :id
          AND (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.dataExclusao IS NULL
        """)
    Optional<Despesa> buscarPorIdEUsuario(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT DISTINCT d FROM Despesa d
        LEFT JOIN FETCH d.conta c
        LEFT JOIN FETCH d.cartao cc
        LEFT JOIN FETCH d.categoria cat
        LEFT JOIN FETCH d.rateios r
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.dataExclusao IS NULL
        ORDER BY d.competencia DESC, d.dataVencimento ASC, d.dataLancamento DESC
        """)
    List<Despesa> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT d FROM Despesa d
        WHERE (d.id = :maeId OR d.parcelaPai.id = :maeId)
          AND d.dataExclusao IS NULL
        ORDER BY d.nroParcela ASC
        """)
    List<Despesa> buscarParcelasDaSerie(@Param("maeId") Long maeId);

    @Query("""
        SELECT d FROM Despesa d
        WHERE (d.id = :maeId OR d.recorrentePai.id = :maeId)
          AND d.dataExclusao IS NULL
        ORDER BY d.competencia ASC
        """)
    List<Despesa> buscarOcorrenciasRecorrentes(@Param("maeId") Long maeId);

    @Query("""
        SELECT COUNT(d) FROM Despesa d
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        WHERE d.id IN :ids
          AND (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.dataExclusao IS NULL
        """)
    long countPorIdsEUsuario(@Param("ids") List<Long> ids, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT d FROM Despesa d
        LEFT JOIN FETCH d.conta c
        LEFT JOIN FETCH d.cartao cc
        WHERE d.id IN :ids
          AND (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.dataExclusao IS NULL
        """)
    List<Despesa> buscarPorIdsEUsuario(@Param("ids") List<Long> ids, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT COALESCE(SUM(d.valor), 0) 
        FROM Despesa d
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.competencia = :competencia
          AND d.dataExclusao IS NULL
    """)
    BigDecimal somarCotaLiquidaPorCompetenciaEUsuario(@Param("competencia") YearMonth competencia, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT d FROM Despesa d
        LEFT JOIN FETCH d.categoria cat
        WHERE d.cartao.id = :cartaoId
          AND d.competencia = :competencia
          AND d.dataExclusao IS NULL
        ORDER BY d.dataVencimento ASC, d.id ASC
        """)
    List<Despesa> listarPorCartaoECompetencia(@Param("cartaoId") Long cartaoId, @Param("competencia") java.time.YearMonth competencia);

    @Query("""
        SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d
        WHERE d.cartao.id = :cartaoId
          AND d.competencia = :competencia
          AND d.dataExclusao IS NULL
        """)
    java.math.BigDecimal somarPorCartaoECompetencia(@Param("cartaoId") Long cartaoId, @Param("competencia") java.time.YearMonth competencia);

    @Query("""
        SELECT DISTINCT d.competencia FROM Despesa d
        WHERE d.cartao.id = :cartaoId
          AND d.dataExclusao IS NULL
        ORDER BY d.competencia ASC
        """)
    List<java.time.YearMonth> listarCompetenciasPorCartao(@Param("cartaoId") Long cartaoId);

    @Query("""
        SELECT COALESCE(cat.nome, 'Sem categoria') AS categoria, 
               cat.cor AS cor,
               COALESCE(SUM(d.valor - COALESCE((SELECT SUM(du.valor) FROM DespesaUsuario du WHERE du.despesa = d AND du.dataExclusao IS NULL), 0)), 0) AS total
        FROM Despesa d
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        LEFT JOIN d.categoria cat
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.competencia = :competencia
          AND d.dataExclusao IS NULL
        GROUP BY cat.id, cat.nome, cat.cor
        ORDER BY total DESC
    """)
    List<Object[]> somarPorCategoriaECompetencia(@Param("competencia") YearMonth competencia, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT COALESCE(d.statusPagamento, 'NAO_SE_APLICA') AS status,
               COALESCE(SUM(d.valor - COALESCE((SELECT SUM(du.valor) FROM DespesaUsuario du WHERE du.despesa = d AND du.dataExclusao IS NULL), 0)), 0) AS total
        FROM Despesa d
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.competencia = :competencia
          AND d.dataExclusao IS NULL
        GROUP BY status
    """)
    List<Object[]> somarPorStatusPagamentoECompetencia(@Param("competencia") YearMonth competencia, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT SUBSTRING(d.competencia, 6, 2) AS mes, COALESCE(SUM(d.valor), 0) AS total
        FROM Despesa d
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND SUBSTRING(d.competencia, 1, 4) = :ano
          AND d.dataExclusao IS NULL
        GROUP BY mes
        ORDER BY mes ASC
    """)
    List<Object[]> somarDespesasPorMesEAnual(@Param("usuarioId") Long usuarioId, @Param("ano") String ano);

    @Query("""
        SELECT SUBSTRING(d.competencia, 1, 4) AS ano, COALESCE(SUM(d.valor), 0) AS total
        FROM Despesa d
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND SUBSTRING(d.competencia, 1, 4) BETWEEN :anoInicio AND :anoFim
          AND d.dataExclusao IS NULL
        GROUP BY ano
        ORDER BY ano ASC
    """)
    List<Object[]> somarDespesasPorIntervaloAnos(@Param("usuarioId") Long usuarioId, @Param("anoInicio") String anoInicio, @Param("anoFim") String anoFim);

    @Query(value = """
        SELECT DISTINCT ano FROM (
          SELECT CAST(SUBSTRING(r.RECE_COMPETENCIA, 1, 4) AS UNSIGNED) AS ano
            FROM receitas r JOIN contas c ON c.CTA_ID = r.CTA_ID
            WHERE c.USU_ID = :usuarioId AND r.audit_data_exclusao IS NULL
          UNION
          SELECT CAST(SUBSTRING(d.DESP_COMPETENCIA, 1, 4) AS UNSIGNED) AS ano
            FROM despesas d
            LEFT JOIN contas c2          ON c2.CTA_ID  = d.CTA_ID
            LEFT JOIN cartoes_credito cc ON cc.CACR_ID = d.CACR_ID
            WHERE (c2.USU_ID = :usuarioId OR cc.USU_ID = :usuarioId) AND d.audit_data_exclusao IS NULL
        ) anos
        ORDER BY ano ASC
    """, nativeQuery = true)
    List<Integer> buscarAnosDisponiveisParaEvolucao(@Param("usuarioId") Long usuarioId);
}
