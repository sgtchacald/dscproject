package br.com.diegocordeiro.dscproject.dto.usuario;

import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import lombok.Getter;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Uma revisão do histórico de um usuário (Hibernate Envers).
 * A senha (e o hash) nunca aparece em nenhuma revisão.
 */
@Getter
public class RevisaoUsuarioDTO {

    private final Integer revisao;
    private final String tipo;
    private final Instant data;
    private final String autor;

    private final String nome;
    private final String login;
    private final String email;
    private final String perfilCodigo;
    private final String generoDescricao;
    private final LocalDate nascimento;

    public RevisaoUsuarioDTO(Revision<Integer, Usuario> revision) {
        this.revisao = revision.getRequiredRevisionNumber();
        this.tipo = traduzirTipo(revision.getMetadata().getRevisionType());
        this.data = revision.getMetadata().getRequiredRevisionInstant();
        Usuario snapshot = revision.getEntity();
        this.autor = snapshot.getAlteradoPor() != null ? snapshot.getAlteradoPor() : snapshot.getCriadoPor();
        this.nome = snapshot.getNome();
        this.login = snapshot.getLogin();
        this.email = snapshot.getEmail();
        this.perfilCodigo = snapshot.getPerfil() != null ? snapshot.getPerfil().getCodigo() : null;
        this.generoDescricao = snapshot.getGenero() != null ? snapshot.getGenero().getDescricao() : null;
        this.nascimento = snapshot.getNascimento();
    }

    private static String traduzirTipo(RevisionMetadata.RevisionType tipo) {
        return switch (tipo) {
            case INSERT -> "Criação";
            case UPDATE -> "Alteração";
            case DELETE -> "Exclusão";
            default -> "Desconhecido";
        };
    }
}
