package br.com.diegocordeiro.dscproject.repository.receita;

import br.com.diegocordeiro.dscproject.model.receita.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    /** A receita só é encontrada quando pertence a uma conta do usuário informado. */
    Optional<Receita> findByIdAndContaUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    /**
     * Listagem para o grid. Não filtra {@code dataExclusao}: a receita
     * excluída continua aparecendo na grid, com a situação "Excluída".
     */
    @Query("""
        SELECT r FROM Receita r
        JOIN FETCH r.conta c
        LEFT JOIN FETCH r.categoria cat
        WHERE c.usuario.id = :usuarioId
        ORDER BY r.competencia DESC, r.dataLancamento DESC
        """)
    List<Receita> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT COALESCE(SUM(r.valor), 0) FROM Receita r
        JOIN r.conta c
        WHERE c.usuario.id = :usuarioId
          AND r.competencia = :competencia
          AND r.dataExclusao IS NULL
        """)
    BigDecimal somarPorCompetenciaEUsuario(@Param("competencia") YearMonth competencia, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT SUBSTRING(r.competencia, 6, 2) AS mes, COALESCE(SUM(r.valor), 0) AS total
        FROM Receita r
        JOIN r.conta c
        WHERE c.usuario.id = :usuarioId
          AND SUBSTRING(r.competencia, 1, 4) = :ano
          AND r.dataExclusao IS NULL
        GROUP BY mes
        ORDER BY mes ASC
    """)
    List<Object[]> somarReceitasPorMesEAnual(@Param("usuarioId") Long usuarioId, @Param("ano") String ano);

    @Query("""
        SELECT SUBSTRING(r.competencia, 1, 4) AS ano, COALESCE(SUM(r.valor), 0) AS total
        FROM Receita r
        JOIN r.conta c
        WHERE c.usuario.id = :usuarioId
          AND SUBSTRING(r.competencia, 1, 4) BETWEEN :anoInicio AND :anoFim
          AND r.dataExclusao IS NULL
        GROUP BY ano
        ORDER BY ano ASC
    """)
    List<Object[]> somarReceitasPorIntervaloAnos(@Param("usuarioId") Long usuarioId, @Param("anoInicio") String anoInicio, @Param("anoFim") String anoFim);
}

