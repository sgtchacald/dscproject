package br.com.diegocordeiro.dscproject.repository.categoria;

import br.com.diegocordeiro.dscproject.model.categoria.CategoriaProvedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaProvedorRepository extends JpaRepository<CategoriaProvedor, Long> {

    @Query("""
        SELECT cp FROM CategoriaProvedor cp
        JOIN FETCH cp.provedor p
        JOIN FETCH cp.categoria c
        WHERE cp.dataExclusao IS NULL
          AND (:provedorId IS NULL OR p.id = :provedorId)
        ORDER BY p.nome ASC, cp.rotuloExterno ASC
        """)
    List<CategoriaProvedor> listarPorProvedor(@Param("provedorId") Long provedorId);

    @Query("""
        SELECT COUNT(cp) FROM CategoriaProvedor cp
        WHERE cp.dataExclusao IS NULL
          AND cp.provedor.id = :provedorId
          AND cp.rotuloExterno = :rotuloExterno
          AND (:idAtual IS NULL OR cp.id <> :idAtual)
        """)
    long contarPorProvedorERotulo(@Param("provedorId") Long provedorId, @Param("rotuloExterno") String rotuloExterno, @Param("idAtual") Long idAtual);
}
