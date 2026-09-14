package br.com.diegocordeiro.dscproject.model.despesa;

import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.comum.LancamentoFinanceiro;
import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "DESPESAS")
@AttributeOverrides({
    @AttributeOverride(name = "competencia", column = @Column(name = "DESP_COMPETENCIA", length = 7, nullable = false)),
    @AttributeOverride(name = "valor", column = @Column(name = "DESP_VALOR", precision = 15, scale = 2, nullable = false)),
    @AttributeOverride(name = "dataLancamento", column = @Column(name = "DESP_DT_LANCAMENTO", nullable = false)),
    @AttributeOverride(name = "origem", column = @Column(name = "DESP_ORIGEM", length = 20, nullable = false))
})
public class Despesa extends LancamentoFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DESP_ID")
    private Long id;

    @Column(name = "DESP_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "DESP_DESCRICAO", length = 512)
    private String descricao;

    @Column(name = "DESP_DT_VENCIMENTO")
    private LocalDate dataVencimento;

    @Column(name = "DESP_DT_PAGAMENTO")
    private LocalDate dataPagamento;

    @Column(name = "DESP_VALOR_TOTAL_COMPRA", precision = 15, scale = 2)
    private BigDecimal valorTotalCompra;

    @Column(name = "DESP_FL_PARCELADA", nullable = false)
    private boolean parcelada = false;

    @Column(name = "DESP_NRO_PARCELA")
    private Integer nroParcela;

    @Column(name = "DESP_QTD_PARCELAS")
    private Integer qtdParcelas;

    @Enumerated(EnumType.STRING)
    @Column(name = "DESP_MEIO_PAGAMENTO", length = 20)
    private MeioPagamento meioPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "DESP_IND_STATUS_PAGAMENTO", length = 20)
    private StatusPagamento statusPagamento;

    @Column(name = "DESP_FL_PAGAMENTO_FATURA", nullable = false)
    private boolean pagamentoFatura = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESP_ID_PARCELA_PAI")
    private Despesa parcelaPai;

    @Column(name = "DESP_FL_RECORRENTE", nullable = false)
    private boolean recorrente = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESP_ID_RECORRENTE_PAI")
    private Despesa recorrentePai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CACR_ID")
    private CartaoCredito cartao;

    @OneToMany(mappedBy = "despesa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DespesaUsuario> rateios = new ArrayList<>();
}
