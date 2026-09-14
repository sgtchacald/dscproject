package br.com.diegocordeiro.dscproject.dto.usuario;

import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Linha do grid de listagem. Nunca expõe a senha.
 */
@Getter
public class UsuarioListaDTO {

    private final Long id;
    private final String nome;
    private final String login;
    private final String email;
    private final String perfilCodigo;
    private final String perfilNome;
    private final String generoDescricao;
    private final LocalDate criadoEm;
    private final boolean excluido;

    public UsuarioListaDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.email = usuario.getEmail();
        this.perfilCodigo = usuario.getPerfil() != null ? usuario.getPerfil().getCodigo() : null;
        this.perfilNome = usuario.getPerfil() != null ? usuario.getPerfil().getNome() : null;
        this.generoDescricao = usuario.getGenero() != null ? usuario.getGenero().getDescricao() : null;
        this.criadoEm = usuario.getDataCriacao() != null
            ? LocalDate.ofInstant(usuario.getDataCriacao(), ZoneId.systemDefault())
            : null;
        this.excluido = usuario.isExcluido();
    }
}
