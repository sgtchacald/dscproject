package br.com.diegocordeiro.dscproject.repository.perfil;

import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilPermissaoRepository extends JpaRepository<PerfilPermissao, Long> {

    boolean existsByPerfilAndPermissao(Perfil perfil, Permissao permissao);

    /** Vínculo existente (ativo ou já excluído) — a constraint única impede par duplicado, então reativamos. */
    Optional<PerfilPermissao> findByPerfilAndPermissao(Perfil perfil, Permissao permissao);

    List<PerfilPermissao> findByPerfilAndDataExclusaoIsNull(Perfil perfil);

    /** Códigos das permissões atualmente vinculadas a um perfil. */
    @Query("""
        SELECT pp.permissao.codigo FROM PerfilPermissao pp
        WHERE pp.perfil.id = :perfilId AND pp.dataExclusao IS NULL
        """)
    List<String> buscarCodigosPermissaoAtivos(@Param("perfilId") Long perfilId);
}
