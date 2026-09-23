package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum StatusConexao {
    ATUALIZANDO("Atualizando", "bg-azure-lt", "ph ph-arrows-clockwise"),
    ATUALIZADO("Atualizado", "bg-success-lt", "ph ph-check-circle"),
    ERRO_LOGIN("Erro de Login", "bg-danger-lt", "ph ph-warning-circle"),
    DESATUALIZADO("Desatualizado", "bg-warning-lt", "ph ph-clock"),
    AGUARDANDO_USUARIO("Aguardando Usuário", "bg-secondary-lt", "ph ph-user");

    private final String descricao;
    private final String badgeClass;
    private final String icone;

    StatusConexao(String descricao, String badgeClass, String icone) {
        this.descricao = descricao;
        this.badgeClass = badgeClass;
        this.icone = icone;
    }
}
