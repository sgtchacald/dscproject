package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum NaturezaMovimento {

    CREDITO("CREDITO", "Crédito", 1),
    DEBITO("DEBITO", "Débito", -1);

    private final String codigo;
    private final String descricao;
    private final int polaridade;

    NaturezaMovimento(String codigo, String descricao, int polaridade) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.polaridade = polaridade;
    }

    public static NaturezaMovimento porCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        for (NaturezaMovimento nat : values()) {
            if (nat.name().equalsIgnoreCase(codigo) || nat.codigo.equalsIgnoreCase(codigo)) {
                return nat;
            }
        }
        throw new IllegalArgumentException("Natureza de movimento inválida: " + codigo);
    }
}
