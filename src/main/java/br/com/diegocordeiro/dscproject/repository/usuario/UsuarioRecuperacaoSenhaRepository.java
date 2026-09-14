package br.com.diegocordeiro.dscproject.repository.usuario;

import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRecuperacaoSenhaRepository extends JpaRepository<UsuarioRecuperacaoSenha, Long> {

    /** Busca o registro pelo hash do token. */
    Optional<UsuarioRecuperacaoSenha> findByTokenHashAndDataExclusaoIsNull(String tokenHash);

    /** Tokens pendentes do usuário, a invalidar a cada nova solicitação. */
    List<UsuarioRecuperacaoSenha> findByUsuarioIdAndUtilizadoFalseAndDataExclusaoIsNull(Long usuarioId);

    /** Quantas solicitações o usuário fez desde {@code limite} (controle de flood). */
    long countByUsuarioIdAndDataCriacaoAfter(Long usuarioId, Instant limite);
}
