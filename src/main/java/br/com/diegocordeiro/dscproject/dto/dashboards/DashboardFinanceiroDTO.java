package br.com.diegocordeiro.dscproject.dto.dashboards;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    public void setTotalDespesas(BigDecimal totalDespesas) {
        this.totalDespesas = totalDespesas;
    }


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

    public static class CategoriaResumoDTO {
        private String categoria;
        private String cor;
        private BigDecimal total;

        public CategoriaResumoDTO(String categoria, String cor, BigDecimal total) {
            this.categoria = categoria;
            this.cor = cor != null ? cor : "#69747f"; // Cor padrão caso venha nula
            this.total = total != null ? total : BigDecimal.ZERO;
        }

        public String getCategoria() { return categoria; }
        public String getCor() { return cor; }
        public BigDecimal getTotal() { return total; }
    }
    // Card 4: Despesas por Categoria
    private List<CategoriaResumoDTO> despesasPorCategoria;

    public List<CategoriaResumoDTO> getDespesasPorCategoria() {
        return despesasPorCategoria;
    }

    public void setDespesasPorCategoria(List<CategoriaResumoDTO> despesasPorCategoria) {
        this.despesasPorCategoria = despesasPorCategoria;
    }

    // Campo para o Gráfico de Rosca do Card 4
    private String conicGradientDespesas;

    public String getConicGradientDespesas() {
        return conicGradientDespesas;
    }

    public void setConicGradientDespesas(String conicGradientDespesas) {
        this.conicGradientDespesas = conicGradientDespesas;
    }

    // Card 5: Pagas vs Pendentes
    private StatusPagamentoResumoDTO statusPagamento;

    public StatusPagamentoResumoDTO getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamentoResumoDTO statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public static class StatusPagamentoResumoDTO {
        private BigDecimal pagas = BigDecimal.ZERO;
        private BigDecimal pendentes = BigDecimal.ZERO;
        private BigDecimal naoSeAplica = BigDecimal.ZERO;

        private int pctPagas = 0;
        private int pctPendentes = 0;
        private int pctNaoSeAplica = 0;

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

    // Card 6: Total Rateado por Pessoa
    private List<RateioPorContatoDTO> rateioPorContato;

    public List<RateioPorContatoDTO> getRateioPorContato() {
        return rateioPorContato;
    }

    public void setRateioPorContato(List<RateioPorContatoDTO> rateioPorContato) {
        this.rateioPorContato = rateioPorContato;
    }

    // dto representa cada linha do Card 6 (Rateio por Pessoa)
    public static class RateioPorContatoDTO {
        private Long contatoId;
        private String nome;
        private BigDecimal total;
        private int percentual; //  usado para desenhar o tamanho da barra roxa na view

        public RateioPorContatoDTO(Long contatoId, String nome, BigDecimal total) {
            this.contatoId = contatoId;
            this.nome = nome != null ? nome : "Contato sem nome";
            this.total = total != null ? total : BigDecimal.ZERO;
            this.percentual = 0; // calculado no Service
        }

        public Long getContatoId() { return contatoId; }
        public void setContatoId(Long contatoId) { this.contatoId = contatoId; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        public int getPercentual() { return percentual; }
        public void setPercentual(int percentual) { this.percentual = percentual; }
    }

    // card 7: Limite de Cartão de Crédito
    private List<CartaoLimiteResumoDTO> cartoesLimite;

    public List<CartaoLimiteResumoDTO> getCartoesLimite() { return cartoesLimite; }
    public void setCartoesLimite(List<CartaoLimiteResumoDTO> cartoesLimite) { this.cartoesLimite = cartoesLimite; }

    // sub-DTO para o Card 7 (Limite por Cartão)
    public static class CartaoLimiteResumoDTO {
        private Long cartaoId;
        private String descricao;
        private BigDecimal limite;
        private BigDecimal usado;
        private BigDecimal disponivel;
        private int percentualUso;
        private String faixaCor; // 'verde', 'amarelo', 'vermelho' ou 'nulo'
        private boolean limiteEstourado;

        // Construtor, Getters e Setters
        public CartaoLimiteResumoDTO(Long cartaoId, String descricao, BigDecimal limite, BigDecimal usado) {
            this.cartaoId = cartaoId;
            this.descricao = descricao;
            this.limite = limite;
            this.usado = usado != null ? usado : BigDecimal.ZERO;
            calcularRegrasDeLimite();
        }

        private void calcularRegrasDeLimite() {
            if (this.limite == null || this.limite.compareTo(BigDecimal.ZERO) <= 0) {
                // RN11: Quando o limite não está definido, não há cálculo de disponível ou barra
                this.faixaCor = "nulo";
                this.limiteEstourado = false;
                this.disponivel = null;
                this.percentualUso = 0;
            } else {
                // Calcula o disponível: Limite - Usado
                this.disponivel = this.limite.subtract(this.usado);

                // Calcula o percentual de uso (Usado * 100 / Limite)
                this.percentualUso = this.usado.multiply(new BigDecimal("100"))
                        .divide(this.limite, 0, java.math.RoundingMode.HALF_UP)
                        .intValue();

                // RN11: Definição das faixas de cores e estouro
                if (this.usado.compareTo(this.limite) > 0) {
                    this.faixaCor = "vermelho";
                    this.limiteEstourado = true;
                    this.percentualUso = 100; // Trava a barra visualmente a 100%
                } else if (this.percentualUso >= 70) {
                    this.faixaCor = "amarelo"; // Entre 70% e 99%
                    this.limiteEstourado = false;
                } else {
                    this.faixaCor = "verde"; // Abaixo de 70%
                    this.limiteEstourado = false;
                }
            }
        }

        // Getters necessários para o Thymeleaf
        public Long getCartaoId() { return cartaoId; }
        public String getDescricao() { return descricao; }
        public BigDecimal getLimite() { return limite; }
        public BigDecimal getUsado() { return usado; }
        public BigDecimal getDisponivel() { return disponivel; }
        public int getPercentualUso() { return percentualUso; }
        public String getFaixaCor() { return faixaCor; }
        public boolean isLimiteEstourado() { return limiteEstourado; }
    }

    // CARD 8: Evolução Receitas × Despesas
    private List<Integer> anosDisponiveis;
    private Integer anoInicioSelecionado;
    private Integer anoFimSelecionado;

    // Listas para os eixos do gráfico (ApexCharts)
    private List<String> rotulosGrafico = new ArrayList<>();       // Meses (Jan-Dez) ou Anos do intervalo
    private List<BigDecimal> receitasGrafico = new ArrayList<>(); // Valores das receitas correspondentes
    private List<BigDecimal> despesasGrafico = new ArrayList<>(); // Valores das despesas correspondentes
    private boolean modoMensal;

    public boolean isModoMensal() {
        return modoMensal;
    }

    public void setModoMensal(boolean modoMensal) {
        this.modoMensal = modoMensal;
    }

    public List<Integer> getAnosDisponiveis() {
        return anosDisponiveis;
    }

    public void setAnosDisponiveis(List<Integer> anosDisponiveis) {
        this.anosDisponiveis = anosDisponiveis;
    }

    public Integer getAnoInicioSelecionado() {
        return anoInicioSelecionado;
    }

    public void setAnoInicioSelecionado(Integer anoInicioSelecionado) {
        this.anoInicioSelecionado = anoInicioSelecionado;
    }

    public Integer getAnoFimSelecionado() {
        return anoFimSelecionado;
    }

    public void setAnoFimSelecionado(Integer anoFimSelecionado) {
        this.anoFimSelecionado = anoFimSelecionado;
    }

    public List<String> getRotulosGrafico() {
        return rotulosGrafico;
    }

    public void setRotulosGrafico(List<String> rotulosGrafico) {
        this.rotulosGrafico = rotulosGrafico;
    }

    public List<BigDecimal> getReceitasGrafico() {
        return receitasGrafico;
    }

    public void setReceitasGrafico(List<BigDecimal> receitasGrafico) {
        this.receitasGrafico = receitasGrafico;
    }

    public List<BigDecimal> getDespesasGrafico() {
        return despesasGrafico;
    }

    public void setDespesasGrafico(List<BigDecimal> despesasGrafico) {
        this.despesasGrafico = despesasGrafico;
    }
}
