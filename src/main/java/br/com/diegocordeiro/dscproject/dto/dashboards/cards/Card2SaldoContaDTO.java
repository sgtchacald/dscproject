package br.com.diegocordeiro.dscproject.dto.dashboards.cards;

import java.math.BigDecimal;
import java.util.List;

public class Card2SaldoContaDTO {
    private List<ContaResumoDTO> contas;

    public Card2SaldoContaDTO(List<ContaResumoDTO> contas) {
        this.contas = contas;
    }

    public List<ContaResumoDTO> getContas() { return contas; }
    public void setContas(List<ContaResumoDTO> contas) { this.contas = contas; }

    public static class ContaResumoDTO {
        private Long id;
        private String descricao;
        private String tipo;
        private BigDecimal saldo;
        private boolean consideraNoSaldoGeral;

        public ContaResumoDTO(Long id, String descricao, String tipo, BigDecimal saldo, boolean consideraNoSaldoGeral) {
            this.id = id;
            this.descricao = descricao;
            this.tipo = tipo;
            this.saldo = saldo;
            this.consideraNoSaldoGeral = consideraNoSaldoGeral;
        }

        public Long getId() { return id; }
        public String getDescricao() { return descricao; }
        public String getTipo() { return tipo; }
        public BigDecimal getSaldo() { return saldo; }
        public boolean isConsideraNoSaldoGeral() { return consideraNoSaldoGeral; }
    }
}