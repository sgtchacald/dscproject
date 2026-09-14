package br.com.diegocordeiro.dscproject.repository.usuario;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long>, RevisionRepository<Usuario, Long, Integer> {

    /** Autenticação por login OU e-mail (AutorizacaoService). */
    Usuario findByLoginOrEmail(String login, String email);

    /** Busca usuário pelo login. */
    Optional<Usuario> findByLogin(String login);

    /** Trava de exclusão de perfil: existe algum usuário (ativo ou excluído) apontando para ele. */
    boolean existsByPerfil(Perfil perfil);

    /** Recuperação de senha: usuário ativo com o e-mail informado. */
    Optional<Usuario> findByEmailAndDataExclusaoIsNull(String email);

    /** Listagem do grid (client-side): traz o perfil no mesmo select, sem filtro de situação. */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.perfil p ORDER BY u.nome ASC")
    List<Usuario> listarParaGrid();

    /**
     * Conta usuários NÃO excluídos cujo login OU e-mail é {@code valor},
     * ignorando o próprio registro quando {@code idAtual} vem preenchido.
     */
    @Query("""
        SELECT COUNT(u) FROM Usuario u
        WHERE u.dataExclusao IS NULL
          AND (u.login = :valor OR u.email = :valor)
          AND (:idAtual IS NULL OR u.id <> :idAtual)
        """)
    long contarPorLoginOuEmail(@Param("valor") String valor, @Param("idAtual") Long idAtual);

    /** Conta os usuários ADMIN ativos. */
    @Query("""
        SELECT COUNT(u) FROM Usuario u
        WHERE u.perfil.codigo = 'ADMIN'
          AND u.dataExclusao IS NULL
        """)
    long contarAdminsAtivos();

    /** Busca usuários ativos para rateio, por parte do nome ou do e-mail. */
    @Query("""
        SELECT u FROM Usuario u
        WHERE u.dataExclusao IS NULL
          AND u.id <> :usuIdLogado
          AND (LOWER(u.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :termo, '%')))
        ORDER BY u.nome ASC
        """)
    List<Usuario> buscarAtivosParaRateio(@Param("termo") String termo, @Param("usuIdLogado") Long usuIdLogado);
}
