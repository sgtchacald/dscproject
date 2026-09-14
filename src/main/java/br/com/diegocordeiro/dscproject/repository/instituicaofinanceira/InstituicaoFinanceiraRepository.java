package br.com.diegocordeiro.dscproject.repository.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituicaoFinanceiraRepository extends JpaRepository<InstituicaoFinanceira, Long> {

    Optional<InstituicaoFinanceira> findByIdAndDataExclusaoIsNull(Long id);

    Optional<InstituicaoFinanceira> findByNomeIgnoreCaseAndDataExclusaoIsNull(String nome);

    Optional<InstituicaoFinanceira> findByCodigoAndDataExclusaoIsNull(String codigo);

    List<InstituicaoFinanceira> findAllByOrderByNomeAsc();

    @Query("""
        SELECT COUNT(i) FROM InstituicaoFinanceira i
        WHERE i.dataExclusao IS NULL
          AND UPPER(TRIM(i.nome)) = UPPER(TRIM(:nome))
          AND (:idAtual IS NULL OR i.id <> :idAtual)
        """)
    long contarPorNome(@Param("nome") String nome, @Param("idAtual") Long idAtual);

    @Query("""
        SELECT COUNT(i) FROM InstituicaoFinanceira i
        WHERE i.dataExclusao IS NULL
          AND i.codigo = :codigo
          AND (:idAtual IS NULL OR i.id <> :idAtual)
        """)
    long contarPorCodigo(@Param("codigo") String codigo, @Param("idAtual") Long idAtual);

    @Query("""
        SELECT i FROM InstituicaoFinanceira i
        WHERE i.dataExclusao IS NULL
          AND i.ativo = true
          AND (:tipo IS NULL OR i.tipo = :tipo)
        ORDER BY i.nome ASC
        """)
    List<InstituicaoFinanceira> listarAtivasPorTipo(@Param("tipo") TipoInstituicaoFinanceira tipo);
}
