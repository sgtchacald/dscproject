package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Card7LimiteCartaoDTO {
    private List<CartaoLimiteResumoDTO> cartoes;

    public Card7LimiteCartaoDTO(List<CartaoLimiteResumoDTO> cartoes) {
        this.cartoes = cartoes;
    }

    public List<CartaoLimiteResumoDTO> getCartoes() { return cartoes; }

    public static class CartaoLimiteResumoDTO {
        private Long cartaoId;
        private String descricao;
        private BigDecimal limite;
        private BigDecimal usado;
        private BigDecimal disponivel;
        private int percentualUso;
        private String faixaCor;
        private boolean limiteEstourado;

        public CartaoLimiteResumoDTO(Long cartaoId, String descricao, BigDecimal limite, BigDecimal usado) {
            this.cartaoId = cartaoId;
            this.descricao = descricao;
            this.limite = limite;
            this.usado = usado != null ? usado : BigDecimal.ZERO;
            calcularRegrasDeLimite();
        }

        private void calcularRegrasDeLimite() {
            if (this.limite == null || this.limite.compareTo(BigDecimal.ZERO) <= 0) {
                this.faixaCor = "nulo";
                this.limiteEstourado = false;
                this.disponivel = null;
                this.percentualUso = 0;
            } else {
                this.disponivel = this.limite.subtract(this.usado);
                this.percentualUso = this.usado.multiply(new BigDecimal("100"))
                        .divide(this.limite, 0, RoundingMode.HALF_UP)
                        .intValue();

                if (this.usado.compareTo(this.limite) > 0) {
                    this.faixaCor = "vermelho";
                    this.limiteEstourado = true;
                    this.percentualUso = 100;
                } else if (this.percentualUso >= 70) {
                    this.faixaCor = "amarelo";
                    this.limiteEstourado = false;
                } else {
                    this.faixaCor = "verde";
                    this.limiteEstourado = false;
                }
            }
        }

        public Long getCartaoId() { return cartaoId; }
        public String getDescricao() { return descricao; }
        public BigDecimal getLimite() { return limite; }
        public BigDecimal getUsado() { return usado; }
        public BigDecimal getDisponivel() { return disponivel; }
        public int getPercentualUso() { return percentualUso; }
        public String getFaixaCor() { return faixaCor; }
        public boolean isLimiteEstourado() { return limiteEstourado; }
    }
}