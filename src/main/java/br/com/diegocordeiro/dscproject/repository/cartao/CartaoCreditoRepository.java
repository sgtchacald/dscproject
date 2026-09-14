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
}
