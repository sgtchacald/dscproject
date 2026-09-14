package br.com.diegocordeiro.dscproject.repository.receita;

import br.com.diegocordeiro.dscproject.model.receita.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
