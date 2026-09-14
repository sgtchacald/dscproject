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
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DespesaEdicaoDTO {

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
    private String formaPagamento;
    private Long contaId;
    private Long cartaoId;
    private MeioPagamento meioPagamento;
    private StatusPagamento statusPagamento;
    private LocalDate dataLancamento;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private OrigemLancamento origem;
    private Long categoriaId;
    private List<DespesaRateioDTO> rateio = new ArrayList<>();

    public DespesaEdicaoDTO(Despesa d) {
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
        if (d.getCartao() != null) {
            this.formaPagamento = "CARTAO";
            this.cartaoId = d.getCartao().getId();
        } else if (d.getConta() != null) {
            if (d.getMeioPagamento() == MeioPagamento.DINHEIRO) {
                this.formaPagamento = "DINHEIRO";
            } else {
                this.formaPagamento = "CONTA";
            }
            this.contaId = d.getConta().getId();
        }
        this.meioPagamento = d.getMeioPagamento();
        this.statusPagamento = d.getStatusPagamento();
        this.dataLancamento = d.getDataLancamento();
        this.dataVencimento = d.getDataVencimento();
        this.dataPagamento = d.getDataPagamento();
        this.origem = d.getOrigem();
        if (d.getCategoria() != null) {
            this.categoriaId = d.getCategoria().getId();
        }
        if (d.getRateios() != null) {
            d.getRateios().stream()
                .filter(r -> r.getDataExclusao() == null && r.getContato() != null)
                .forEach(r -> this.rateio.add(new DespesaRateioDTO(
                    r.getContato().getId(),
                    r.getContato().getNome(),
                    r.getContato().getEmail(),
                    r.getContato().getTipo(),
                    r.getContato().getChavePix(),
                    r.getValor(),
                    r.getStatusPagamento(),
                    r.getDataAcerto()
                )));
        }
    }
}
