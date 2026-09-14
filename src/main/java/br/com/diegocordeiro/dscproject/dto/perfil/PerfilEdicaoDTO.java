package br.com.diegocordeiro.dscproject.dto.perfil;

import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import lombok.Getter;

import java.util.List;

/** Dados de um perfil para o modal de edição, com os códigos das permissões vinculadas. */
@Getter
public class PerfilEdicaoDTO {

    private final Long id;
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final boolean sistema;
    private final List<String> permissoes;

    public PerfilEdicaoDTO(Perfil perfil, List<String> permissoes) {
        this.id = perfil.getId();
        this.codigo = perfil.getCodigo();
        this.nome = perfil.getNome();
        this.descricao = perfil.getDescricao();
        this.sistema = perfil.isSistema();
        this.permissoes = permissoes;
    }
}
