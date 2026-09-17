package br.com.diegocordeiro.dscproject.model.fatura;

import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusFatura;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "FATURAS_CARTAO")
public class FaturaCartao extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FTCA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CACR_ID", nullable = false)
    private CartaoCredito cartao;

    @Column(name = "FTCA_COMPETENCIA", length = 7, nullable = false)
    private YearMonth competencia;

    @Column(name = "FTCA_DT_FECHAMENTO")
    private LocalDate dataFechamento;

    @Column(name = "FTCA_DT_VENCIMENTO", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "FTCA_VALOR_TOTAL", precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "FTCA_VALOR_MINIMO", precision = 15, scale = 2)
    private BigDecimal valorMinimo;

    @Column(name = "FTCA_VALOR_ENCARGOS", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorEncargos = BigDecimal.ZERO;

    @Column(name = "FTCA_VALOR_PAGO", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "FTCA_STATUS", length = 20, nullable = false)
    private StatusFatura status = StatusFatura.ABERTA;

    @Enumerated(EnumType.STRING)
    @Column(name = "FTCA_ORIGEM", length = 20, nullable = false)
    private OrigemLancamento origem = OrigemLancamento.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRBA_ID_PAGAMENTO")
    private TransacaoBancaria transacaoPagamento;
}
