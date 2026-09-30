package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;
import java.util.List;

public class Card4DespesasCategoriaDTO {
    private List<CategoriaResumoDTO> despesasPorCategoria;
    private String conicGradientDespesas;

    public Card4DespesasCategoriaDTO(List<CategoriaResumoDTO> despesasPorCategoria, String conicGradientDespesas) {
        this.despesasPorCategoria = despesasPorCategoria;
        this.conicGradientDespesas = conicGradientDespesas;
    }

    public List<CategoriaResumoDTO> getDespesasPorCategoria() { return despesasPorCategoria; }
    public String getConicGradientDespesas() { return conicGradientDespesas; }

    public static class CategoriaResumoDTO {
        private String categoria;
        private String cor;
        private BigDecimal total;

        public CategoriaResumoDTO(String categoria, String cor, BigDecimal total) {
            this.categoria = categoria;
            this.cor = cor != null ? cor : "#69747f";
            this.total = total != null ? total : BigDecimal.ZERO;
        }

        public String getCategoria() { return categoria; }
        public String getCor() { return cor; }
        public BigDecimal getTotal() { return total; }
    }
}