package br.com.diegocordeiro.dscproject.enums;

import br.com.diegocordeiro.dscproject.model.contato.Contato;
import lombok.Getter;

@Getter
public enum TipoContato {

    EXTERNO("Contato Externo"),
    SISTEMA("Usuário do Sistema");

    private final String descricao;

    TipoContato(String descricao) {
        this.descricao = descricao;
    }
}
