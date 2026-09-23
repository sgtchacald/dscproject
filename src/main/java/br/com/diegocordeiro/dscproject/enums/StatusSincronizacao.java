package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum StatusSincronizacao {
    EXECUTANDO("Executando"),
    CONCLUIDA("Concluída"),
    ERRO_PARCIAL("Erro Parcial"),
    ERRO("Erro");

    private final String descricao;

    StatusSincronizacao(String descricao) {
        this.descricao = descricao;
    }
}
