package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum StatusConciliacao {
    PENDENTE("Pendente"),
    CONCILIADA("Conciliada"),
    IGNORADA("Ignorada");

    private final String descricao;

    StatusConciliacao(String descricao) {
        this.descricao = descricao;
    }
}
