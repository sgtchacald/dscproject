package br.com.diegocordeiro.dscproject.dto.usuario;

import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Dados de um usuário para o modal de edição.
 * A senha (e o hash) nunca é incluída.
 */
@Getter
public class UsuarioEdicaoDTO {

    private final Long id;
    private final String nome;
    private final String genero;
    private final LocalDate nascimento;
    private final String email;
    private final String login;
    private final String perfilCodigo;

    public UsuarioEdicaoDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.genero = usuario.getGenero() != null ? usuario.getGenero().getCodigo() : null;
        this.nascimento = usuario.getNascimento();
        this.email = usuario.getEmail();
        this.login = usuario.getLogin();
        this.perfilCodigo = usuario.getPerfil() != null ? usuario.getPerfil().getCodigo() : null;
    }
}
