package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Card8EvolucaoDTO {
    private List<Integer> anosDisponiveis = new ArrayList<>();
    private Integer anoInicioSelecionado;
    private Integer anoFimSelecionado;
    private List<String> rotulosGrafico = new ArrayList<>();
    private List<BigDecimal> receitasGrafico = new ArrayList<>();
    private List<BigDecimal> despesasGrafico = new ArrayList<>();
    private boolean modoMensal;
    private String competenciaSelecionada;

    public Card8EvolucaoDTO() {}

    public List<Integer> getAnosDisponiveis() { return anosDisponiveis; }
    public void setAnosDisponiveis(List<Integer> anosDisponiveis) { this.anosDisponiveis = anosDisponiveis; }

    public Integer getAnoInicioSelecionado() { return anoInicioSelecionado; }
    public void setAnoInicioSelecionado(Integer anoInicioSelecionado) { this.anoInicioSelecionado = anoInicioSelecionado; }

    public Integer getAnoFimSelecionado() { return anoFimSelecionado; }
    public void setAnoFimSelecionado(Integer anoFimSelecionado) { this.anoFimSelecionado = anoFimSelecionado; }

    public List<String> getRotulosGrafico() { return rotulosGrafico; }
    public void setRotulosGrafico(List<String> rotulosGrafico) { this.rotulosGrafico = rotulosGrafico; }

    public List<BigDecimal> getReceitasGrafico() { return receitasGrafico; }
    public void setReceitasGrafico(List<BigDecimal> receitasGrafico) { this.receitasGrafico = receitasGrafico; }

    public List<BigDecimal> getDespesasGrafico() { return despesasGrafico; }
    public void setDespesasGrafico(List<BigDecimal> despesasGrafico) { this.despesasGrafico = despesasGrafico; }

    public boolean isModoMensal() { return modoMensal; }
    public void setModoMensal(boolean modoMensal) { this.modoMensal = modoMensal; }

    public String getCompetenciaSelecionada() { return competenciaSelecionada; }
    public void setCompetenciaSelecionada(String competenciaSelecionada) { this.competenciaSelecionada = competenciaSelecionada; }
}