package br.com.diegocordeiro.dscproject.dto.permissao;

import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import lombok.Getter;

/** Linha do catálogo de permissões (somente leitura), consumida pela tela e pelo seletor do modal de perfil. */
@Getter
public class PermissaoCatalogoDTO {

    private final String codigo;
    private final String nome;
    private final String modulo;
    private final boolean concedivelPorPlano;
    private final boolean orfa;

    public PermissaoCatalogoDTO(Permissao permissao) {
        this.codigo = permissao.getCodigo();
        this.nome = permissao.getNome();
        this.modulo = permissao.getModulo();
        this.concedivelPorPlano = permissao.isConcedivelPorPlano();
        this.orfa = permissao.isOrfa();
    }
}
