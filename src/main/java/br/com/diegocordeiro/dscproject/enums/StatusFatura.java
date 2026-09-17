package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum StatusFatura {
    ABERTA("Aberta"),
    FECHADA("Fechada"),
    PAGA("Paga"),
    PAGA_PARCIAL("Paga Parcial");

    private final String descricao;

    StatusFatura(String descricao) {
        this.descricao = descricao;
    }
}
