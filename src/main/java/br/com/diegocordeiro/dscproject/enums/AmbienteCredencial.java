package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum AmbienteCredencial {
    SANDBOX("Sandbox (Testes)"),
    PRODUCTION("Produção");

    private final String descricao;

    AmbienteCredencial(String descricao) {
        this.descricao = descricao;
    }
}
