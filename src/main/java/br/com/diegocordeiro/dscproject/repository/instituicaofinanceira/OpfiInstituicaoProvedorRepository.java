package br.com.diegocordeiro.dscproject.repository.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiInstituicaoProvedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpfiInstituicaoProvedorRepository extends JpaRepository<OpfiInstituicaoProvedor, Long> {

    Optional<OpfiInstituicaoProvedor> findByIdAndDataExclusaoIsNull(Long id);

    List<OpfiInstituicaoProvedor> findByInstituicaoIdAndDataExclusaoIsNull(Long instituicaoId);

    long countByInstituicaoIdAndDataExclusaoIsNull(Long instituicaoId);

    @Query("""
        SELECT COUNT(m) FROM OpfiInstituicaoProvedor m
        WHERE m.dataExclusao IS NULL
          AND m.instituicao.id = :instituicaoId
          AND m.provedor.id = :provedorId
          AND (:idAtual IS NULL OR m.id <> :idAtual)
        """)
    long contarPorInstituicaoEProvedor(@Param("instituicaoId") Long instituicaoId, @Param("provedorId") Long provedorId, @Param("idAtual") Long idAtual);

    @Query("""
        SELECT COUNT(m) FROM OpfiInstituicaoProvedor m
        WHERE m.dataExclusao IS NULL
          AND m.provedor.id = :provedorId
          AND m.idExterno = :idExterno
          AND (:idAtual IS NULL OR m.id <> :idAtual)
        """)
    long contarPorProvedorEIdExterno(@Param("provedorId") Long provedorId, @Param("idExterno") String idExterno, @Param("idAtual") Long idAtual);

    @Query("""
        SELECT m FROM OpfiInstituicaoProvedor m
        JOIN FETCH m.provedor p
        JOIN FETCH m.instituicao i
        WHERE m.dataExclusao IS NULL
        ORDER BY p.nome ASC, i.nome ASC
        """)
    List<OpfiInstituicaoProvedor> listarParaGrid();
}
