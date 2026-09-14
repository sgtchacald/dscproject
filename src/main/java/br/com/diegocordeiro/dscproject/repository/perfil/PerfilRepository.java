package br.com.diegocordeiro.dscproject.repository.perfil;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilResumoDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByCodigo(String codigo);

    List<Perfil> findAllByOrderByNomeAsc();

    /** Grid de perfis: contagem de permissões ativas e de usuários ativos por perfil. */
    @Query("""
        SELECT new br.com.diegocordeiro.dscproject.dto.perfil.PerfilResumoDTO(
            p.id, p.codigo, p.nome, p.sistema,
            (SELECT COUNT(pp) FROM PerfilPermissao pp WHERE pp.perfil = p AND pp.dataExclusao IS NULL),
            (SELECT COUNT(u) FROM Usuario u WHERE u.perfil = p AND u.dataExclusao IS NULL))
        FROM Perfil p
        WHERE p.dataExclusao IS NULL
        ORDER BY p.nome ASC
        """)
    List<PerfilResumoDTO> listarResumo();

    /** Conta perfis não excluídos com este código, ignorando o próprio na edição. */
    @Query("""
        SELECT COUNT(p) FROM Perfil p
        WHERE p.dataExclusao IS NULL
          AND p.codigo = :codigo
          AND (:idAtual IS NULL OR p.id <> :idAtual)
        """)
    long contarPorCodigo(@Param("codigo") String codigo, @Param("idAtual") Long idAtual);

    /** Anti-lockout global: perfis não excluídos, com ao menos um usuário ativo, que concedem a permissão. */
    @Query("""
        SELECT COUNT(DISTINCT p.id) FROM Perfil p
        JOIN PerfilPermissao pp ON pp.perfil = p AND pp.dataExclusao IS NULL
        JOIN Permissao pm ON pm = pp.permissao
        JOIN Usuario u ON u.perfil = p AND u.dataExclusao IS NULL
        WHERE p.dataExclusao IS NULL AND pm.codigo = :codigoPermissao
        """)
    long contarPerfisComUsuarioAtivoConcedendo(@Param("codigoPermissao") String codigoPermissao);
}
