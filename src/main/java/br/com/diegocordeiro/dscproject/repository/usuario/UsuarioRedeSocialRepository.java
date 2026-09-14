package br.com.diegocordeiro.dscproject.repository.usuario;

import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRedeSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRedeSocialRepository extends JpaRepository<UsuarioRedeSocial, Long> {

    List<UsuarioRedeSocial> findByUsuarioIdAndDataExclusaoIsNullOrderByTipoAsc(Long usuarioId);

    @Query("SELECT r FROM UsuarioRedeSocial r WHERE r.usuario.id = :usuarioId AND r.ativo = true AND r.dataExclusao IS NULL ORDER BY r.tipo ASC")
    List<UsuarioRedeSocial> buscarAtivasPorUsuario(@Param("usuarioId") Long usuarioId);

    Optional<UsuarioRedeSocial> findByIdAndUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndTipoAndDataExclusaoIsNull(Long usuarioId, TipoRedeSocial tipo);

    boolean existsByUsuarioIdAndTipoAndIdNotAndDataExclusaoIsNull(Long usuarioId, TipoRedeSocial tipo, Long id);
}
