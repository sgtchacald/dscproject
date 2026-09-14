package br.com.diegocordeiro.dscproject.repository.despesa;

import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
