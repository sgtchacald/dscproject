package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum StatusEventoWebhook {
    PENDENTE("Pendente"),
    PROCESSADO("Processado"),
    ERRO("Erro");

    private final String descricao;

    StatusEventoWebhook(String descricao) {
        this.descricao = descricao;
    }
}
