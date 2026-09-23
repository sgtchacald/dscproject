package br.com.diegocordeiro.dscproject.repository.despesa;

import br.com.diegocordeiro.dscproject.model.despesa.DespesaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface DespesaUsuarioRepository extends JpaRepository<DespesaUsuario, Long> {

    @Query("""
        SELECT du FROM DespesaUsuario du
        JOIN FETCH du.contato c
        WHERE du.despesa.id = :despId
          AND du.dataExclusao IS NULL
        ORDER BY c.nome ASC
        """)
    List<DespesaUsuario> findByDespesaIdAndDataExclusaoIsNull(@Param("despId") Long despId);

    Optional<DespesaUsuario> findByDespesaIdAndContatoIdAndDataExclusaoIsNull(Long despId, Long contatoId);


    @Query("""
        SELECT ct.id, ct.nome, COALESCE(SUM(du.valor), 0) AS total
        FROM DespesaUsuario du
        JOIN du.despesa d
        JOIN du.contato ct
        LEFT JOIN d.conta c
        LEFT JOIN d.cartao cc
        WHERE (c.usuario.id = :usuarioId OR cc.usuario.id = :usuarioId)
          AND d.competencia = :competencia
          AND d.dataExclusao IS NULL
          AND du.dataExclusao IS NULL
        GROUP BY ct.id, ct.nome
        ORDER BY total DESC
    """)
    List<Object[]> somarRateioPorContatoECompetencia(@Param("competencia") YearMonth competencia, @Param("usuarioId") Long usuarioId);
}
