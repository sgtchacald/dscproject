package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum TipoContaExterna {
    BANK("Conta Bancária"),
    CREDIT("Cartão de Crédito");

    private final String descricao;

    TipoContaExterna(String descricao) {
        this.descricao = descricao;
    }
}
