package br.com.diegocordeiro.dscproject.repository.despesa;

import br.com.diegocordeiro.dscproject.model.despesa.DespesaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
