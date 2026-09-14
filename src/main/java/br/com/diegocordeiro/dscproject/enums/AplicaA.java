package br.com.diegocordeiro.dscproject.enums;

import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.receita.Receita;
import lombok.Getter;

/**
 * Restringe a aplicabilidade da categoria nos registros financeiros.
 */
@Getter
public enum AplicaA {

    RECEITA("Receita"),
    DESPESA("Despesa"),
    AMBOS("Ambos");

    private final String descricao;

    AplicaA(String descricao) {
        this.descricao = descricao;
    }
}
