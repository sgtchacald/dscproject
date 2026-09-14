package br.com.diegocordeiro.dscproject.repository.categoria;

import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByCodigoAndDataExclusaoIsNull(String codigo);

    Optional<Categoria> findByIdAndDataExclusaoIsNull(Long id);

    @Query("""
        SELECT COUNT(c) FROM Categoria c
        WHERE c.dataExclusao IS NULL
          AND c.codigo = :codigo
          AND (:idAtual IS NULL OR c.id <> :idAtual)
        """)
    long contarPorCodigo(@Param("codigo") String codigo, @Param("idAtual") Long idAtual);

    @Query("""
        SELECT c FROM Categoria c
        WHERE c.dataExclusao IS NULL
          AND c.ativo = true
          AND (:aplicaA IS NULL OR c.aplicaA = :aplicaA OR c.aplicaA = br.com.diegocordeiro.dscproject.enums.AplicaA.AMBOS)
        ORDER BY c.nome ASC
        """)
    List<Categoria> listarAtivasPorAplicaA(@Param("aplicaA") AplicaA aplicaA);

    List<Categoria> findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc();

    @Query("""
        SELECT c FROM Categoria c
        WHERE (:busca IS NULL OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%')))
          AND (:aplicaA IS NULL OR c.aplicaA = :aplicaA)
          AND (:sistema IS NULL OR c.sistema = :sistema)
          AND (
              (:situacao = 'TODAS')
              OR (:situacao = 'ATIVA' AND c.ativo = true AND c.dataExclusao IS NULL)
              OR (:situacao = 'INATIVA' AND c.ativo = false AND c.dataExclusao IS NULL)
              OR (:situacao = 'EXCLUIDA' AND c.dataExclusao IS NOT NULL)
          )
        ORDER BY c.nome ASC
        """)
    List<Categoria> listarComFiltros(@Param("busca") String busca, @Param("aplicaA") AplicaA aplicaA, @Param("sistema") Boolean sistema, @Param("situacao") String situacao);
}
