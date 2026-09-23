package br.com.diegocordeiro.dscproject.repository.cartao;

import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long> {

    Optional<CartaoCredito> findByIdAndUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    @Query("""
        SELECT c FROM CartaoCredito c
        LEFT JOIN FETCH c.conta
        WHERE c.usuario.id = :usuarioId
          AND c.dataExclusao IS NULL
        ORDER BY c.descricao ASC
        """)
    List<CartaoCredito> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT c FROM CartaoCredito c
        WHERE c.usuario.id = :usuarioId
          AND c.dataExclusao IS NULL
          AND c.ativo = true
        ORDER BY c.descricao ASC
        """)
    List<CartaoCredito> listarAtivasPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT ca.id, ca.descricao, ca.limite,
               COALESCE((SELECT SUM(d.valor) FROM Despesa d 
                         WHERE d.cartao.id = ca.id 
                           AND d.statusPagamento = 'NAO' 
                           AND d.dataExclusao IS NULL), 0) AS usado
        FROM CartaoCredito ca
        WHERE ca.usuario.id = :usuarioId
          AND ca.ativo = TRUE
          AND ca.dataExclusao IS NULL
        ORDER BY ca.descricao ASC
    """)
    List<Object[]> buscarLimiteUsadoPorCartao(@Param("usuarioId") Long usuarioId);
}
