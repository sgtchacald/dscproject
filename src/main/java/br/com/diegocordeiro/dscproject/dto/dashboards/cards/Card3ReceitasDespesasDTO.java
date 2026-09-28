package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;

public class Card3ReceitasDespesasDTO {
    private BigDecimal totalReceitas = BigDecimal.ZERO;
    private BigDecimal totalDespesas = BigDecimal.ZERO;

    public Card3ReceitasDespesasDTO(BigDecimal totalReceitas, BigDecimal totalDespesas) {
        this.totalReceitas = totalReceitas != null ? totalReceitas : BigDecimal.ZERO;
        this.totalDespesas = totalDespesas != null ? totalDespesas : BigDecimal.ZERO;
    }

    public BigDecimal getTotalReceitas() { return totalReceitas; }
    public BigDecimal getTotalDespesas() { return totalDespesas; }
}