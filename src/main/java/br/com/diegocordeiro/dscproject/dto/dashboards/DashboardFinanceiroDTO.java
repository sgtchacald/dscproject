package br.com.diegocordeiro.dscproject.dto.dashboards;

import java.math.BigDecimal;
import java.util.List;

public class DashboardFinanceiroDTO {
    private String competenciaSelecionada;

    // Card 1 Saldo Consolidado
    private BigDecimal saldoConsolidado = BigDecimal.ZERO;

    // Card 2
    private List<ContaResumoDTO> contas;

    public DashboardFinanceiroDTO() {}

    public String getCompetenciaSelecionada() {
        return competenciaSelecionada;
    }

    public void setCompetenciaSelecionada(String competenciaSelecionada) {
        this.competenciaSelecionada = competenciaSelecionada;
    }

    public BigDecimal getSaldoConsolidado() {
        return saldoConsolidado;
    }

    public void setSaldoConsolidado(BigDecimal saldoConsolidado) {
        this.saldoConsolidado = saldoConsolidado;
    }

    public List<ContaResumoDTO> getContas() {
        return contas;
    }

    public void setContas(List<ContaResumoDTO> contas) {
        this.contas = contas;
    }

    //Card 3
    private BigDecimal totalReceitas = BigDecimal.ZERO;

    public BigDecimal getTotalReceitas() {
        return totalReceitas;
    }

    public void setTotalReceitas(BigDecimal totalReceitas) {
        this.totalReceitas = totalReceitas;
    }

    private BigDecimal totalDespesas = BigDecimal.ZERO;

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public void setTotalDespesas(BigDecimal totalDespesas) {}


    // Sub-DTO interno para representar cada linha do Card 2 (Saldo por Conta)
    public static class ContaResumoDTO {
        private Long id;
        private String descricao;
        private String tipo;
        private BigDecimal saldo;
        private boolean consideraNoSaldoGeral; // Para exibir o badge se for false

        public ContaResumoDTO(Long id, String descricao, String tipo, BigDecimal saldo, boolean consideraNoSaldoGeral) {
            this.id = id;
            this.descricao = descricao;
            this.tipo = tipo;
            this.saldo = saldo;
            this.consideraNoSaldoGeral = consideraNoSaldoGeral;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getSaldo() {
            return saldo;
        }

        public void setSaldo(BigDecimal saldo) {
            this.saldo = saldo;
        }

        public boolean isConsideraNoSaldoGeral() {
            return consideraNoSaldoGeral;
        }

        public void setConsideraNoSaldoGeral(boolean consideraNoSaldoGeral) {
            this.consideraNoSaldoGeral = consideraNoSaldoGeral;
        }
    }
}
