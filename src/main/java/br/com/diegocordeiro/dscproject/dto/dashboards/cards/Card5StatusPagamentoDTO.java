package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;

public class Card5StatusPagamentoDTO {
    private BigDecimal pagas = BigDecimal.ZERO;
    private BigDecimal pendentes = BigDecimal.ZERO;
    private BigDecimal naoSeAplica = BigDecimal.ZERO;

    private int pctPagas = 0;
    private int pctPendentes = 0;
    private int pctNaoSeAplica = 0;

    public Card5StatusPagamentoDTO() {}

    public BigDecimal getPagas() { return pagas; }
    public void setPagas(BigDecimal pagas) { this.pagas = pagas; }

    public BigDecimal getPendentes() { return pendentes; }
    public void setPendentes(BigDecimal pendentes) { this.pendentes = pendentes; }

    public BigDecimal getNaoSeAplica() { return naoSeAplica; }
    public void setNaoSeAplica(BigDecimal naoSeAplica) { this.naoSeAplica = naoSeAplica; }

    public int getPctPagas() { return pctPagas; }
    public void setPctPagas(int pctPagas) { this.pctPagas = pctPagas; }

    public int getPctPendentes() { return pctPendentes; }
    public void setPctPendentes(int pctPendentes) { this.pctPendentes = pctPendentes; }

    public int getPctNaoSeAplica() { return pctNaoSeAplica; }
    public void setPctNaoSeAplica(int pctNaoSeAplica) { this.pctNaoSeAplica = pctNaoSeAplica; }
}