package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;
import java.util.List;

public class Card6RateioContatoDTO {
    private List<RateioPorContatoDTO> rateios;

    public Card6RateioContatoDTO(List<RateioPorContatoDTO> rateios) {
        this.rateios = rateios;
    }

    public List<RateioPorContatoDTO> getRateios() { return rateios; }

    public static class RateioPorContatoDTO {
        private Long contatoId;
        private String nome;
        private BigDecimal total;
        private int percentual;

        public RateioPorContatoDTO(Long contatoId, String nome, BigDecimal total) {
            this.contatoId = contatoId;
            this.nome = nome != null ? nome : "Contato sem nome";
            this.total = total != null ? total : BigDecimal.ZERO;
            this.percentual = 0;
        }

        public Long getContatoId() { return contatoId; }
        public String getNome() { return nome; }
        public BigDecimal getTotal() { return total; }

        public int getPercentual() { return percentual; }
        public void setPercentual(int percentual) { this.percentual = percentual; }
    }
}