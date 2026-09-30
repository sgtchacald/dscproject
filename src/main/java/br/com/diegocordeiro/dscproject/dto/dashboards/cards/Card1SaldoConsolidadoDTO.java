package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;

public class Card1SaldoConsolidadoDTO {
    private BigDecimal saldoConsolidado = BigDecimal.ZERO;

    public Card1SaldoConsolidadoDTO(BigDecimal saldoConsolidado) {
        this.saldoConsolidado = saldoConsolidado != null ? saldoConsolidado : BigDecimal.ZERO;
    }

    public BigDecimal getSaldoConsolidado() { return saldoConsolidado; }
    public void setSaldoConsolidado(BigDecimal saldoConsolidado) { this.saldoConsolidado = saldoConsolidado; }
}