package br.com.diegocordeiro.dscproject.repository.conta;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByIdAndUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    Optional<Conta> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
        SELECT c FROM Conta c
        JOIN FETCH c.instituicao i
        WHERE c.usuario.id = :usuarioId
          AND c.dataExclusao IS NULL
        ORDER BY c.descricao ASC
        """)
    List<Conta> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT COUNT(c) FROM Conta c
        WHERE c.dataExclusao IS NULL
          AND c.usuario.id = :usuarioId
          AND UPPER(c.descricao) = UPPER(:descricao)
          AND (:idAtual IS NULL OR c.id <> :idAtual)
        """)
    long contarPorUsuarioEDescricao(@Param("usuarioId") Long usuarioId, @Param("descricao") String descricao, @Param("idAtual") Long idAtual);

    @Query("""
        SELECT c FROM Conta c
        JOIN FETCH c.instituicao i
        WHERE c.dataExclusao IS NULL
          AND c.usuario.id = :usuarioId
          AND c.ativo = true
        ORDER BY c.descricao ASC
        """)
    List<Conta> listarAtivasPorUsuario(@Param("usuarioId") Long usuarioId);
}
