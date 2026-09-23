package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum StatusConsentimento {
    ATIVO("Ativo"),
    EXPIRADO("Expirado"),
    REVOGADO("Revogado"),
    PENDENTE("Pendente");

    private final String descricao;

    StatusConsentimento(String descricao) {
        this.descricao = descricao;
    }
}
