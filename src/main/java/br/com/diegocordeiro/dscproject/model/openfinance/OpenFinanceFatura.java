package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusConciliacao;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_FATURAS")
public class OpenFinanceFatura extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFFA_ID")
    private Long id;

    @Column(name = "OFFA_ID_EXTERNO", length = 60, unique = true)
    private String idExterno;

    @Column(name = "OFFA_DT_VENCIMENTO", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "OFFA_DT_FECHAMENTO")
    private LocalDate dataFechamento;

    @Column(name = "OFFA_VALOR_TOTAL", precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "OFFA_VALOR_MINIMO", precision = 15, scale = 2)
    private BigDecimal valorMinimo;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFFA_STATUS_CONCILIACAO", length = 20, nullable = false)
    private StatusConciliacao statusConciliacao = StatusConciliacao.PENDENTE;

    @Column(name = "OFFA_ID_FATURA_GERADA")
    private Long idFaturaGerada;

    @Column(name = "OFFA_DADOS_BRUTOS", columnDefinition = "json")
    private String dadosBrutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCE_ID", nullable = false)
    private OpenFinanceContaExterna contaExterna;
}
