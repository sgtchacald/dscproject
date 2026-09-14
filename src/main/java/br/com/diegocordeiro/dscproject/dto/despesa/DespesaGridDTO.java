package br.com.diegocordeiro.dscproject.dto.despesa;

import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DespesaGridDTO {

    private Long id;
    private String competencia;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private BigDecimal valorTotalCompra;
    private boolean parcelada;
    private Integer nroParcela;
    private Integer qtdParcelas;
    private Long idParcelaPai;
    private boolean recorrente;
    private Long idRecorrentePai;
    private MeioPagamento meioPagamento;
    private StatusPagamento statusPagamento;
    private LocalDate dataLancamento;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private OrigemLancamento origem;
    private String formaPagamento;
    private Long contaId;
    private String contaDescricao;
    private Long cartaoId;
    private String cartaoDescricao;
    private Long categoriaId;
    private String categoriaNome;
    private boolean temRateio;
    private int qtdCoParticipantes;
    private boolean excluido;

    public DespesaGridDTO(Despesa d) {
        this.id = d.getId();
        this.competencia = d.getCompetencia() != null ? d.getCompetencia().toString() : "";
        this.nome = d.getNome();
        this.descricao = d.getDescricao();
        this.valor = d.getValor();
        this.valorTotalCompra = d.getValorTotalCompra();
        this.parcelada = d.isParcelada();
        this.nroParcela = d.getNroParcela();
        this.qtdParcelas = d.getQtdParcelas();
        this.idParcelaPai = d.getParcelaPai() != null ? d.getParcelaPai().getId() : null;
        this.recorrente = d.isRecorrente();
        this.idRecorrentePai = d.getRecorrentePai() != null ? d.getRecorrentePai().getId() : null;
        this.meioPagamento = d.getMeioPagamento();
        this.statusPagamento = d.getStatusPagamento();
        this.dataLancamento = d.getDataLancamento();
        this.dataVencimento = d.getDataVencimento();
        this.dataPagamento = d.getDataPagamento();
        this.origem = d.getOrigem();
        if (d.getCartao() != null) {
            this.formaPagamento = "CARTAO";
            this.cartaoId = d.getCartao().getId();
            this.cartaoDescricao = d.getCartao().getDescricao();
        } else if (d.getMeioPagamento() == MeioPagamento.DINHEIRO) {
            this.formaPagamento = "DINHEIRO";
            if (d.getConta() != null) {
                this.contaId = d.getConta().getId();
                this.contaDescricao = d.getConta().getDescricao();
            }
        } else if (d.getConta() != null) {
            this.formaPagamento = "CONTA";
            this.contaId = d.getConta().getId();
            this.contaDescricao = d.getConta().getDescricao();
        }
        if (d.getCategoria() != null) {
            this.categoriaId = d.getCategoria().getId();
            this.categoriaNome = d.getCategoria().getNome();
        }
        this.qtdCoParticipantes = d.getRateios() != null
                ? (int) d.getRateios().stream().filter(r -> r.getDataExclusao() == null).count()
                : 0;
        this.temRateio = this.qtdCoParticipantes > 0;
        this.excluido = d.getDataExclusao() != null;
    }
}
