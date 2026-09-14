package br.com.diegocordeiro.dscproject.repository.contato;

import br.com.diegocordeiro.dscproject.enums.StatusContato;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {

    Optional<Contato> findByIdAndUsuarioDonoIdAndDataExclusaoIsNull(Long id, Long usuarioDonoId);

    @Query("""
        SELECT c FROM Contato c
        WHERE c.usuarioDono.id = :usuarioDonoId
          AND c.status = br.com.diegocordeiro.dscproject.enums.StatusContato.ATIVO
          AND c.dataExclusao IS NULL
          AND (
              LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
              OR (c.email IS NOT NULL AND LOWER(c.email) LIKE LOWER(CONCAT('%', :termo, '%')))
              OR (c.telefone IS NOT NULL AND c.telefone LIKE CONCAT('%', :termo, '%'))
          )
        ORDER BY c.nome ASC
        """)
    List<Contato> buscarAtivosPorDonoETermo(@Param("usuarioDonoId") Long usuarioDonoId, @Param("termo") String termo);

    @Query("""
        SELECT c FROM Contato c
        WHERE c.usuarioDono.id = :usuarioDonoId
          AND c.dataExclusao IS NULL
        ORDER BY c.nome ASC
        """)
    List<Contato> listarPorUsuarioDono(@Param("usuarioDonoId") Long usuarioDonoId);
}
