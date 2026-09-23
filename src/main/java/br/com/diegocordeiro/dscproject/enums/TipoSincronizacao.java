package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum TipoSincronizacao {
    MANUAL("Manual"),
    AGENDADA("Agendada"),
    WEBHOOK("Webhook");

    private final String descricao;

    TipoSincronizacao(String descricao) {
        this.descricao = descricao;
    }
}
