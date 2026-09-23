package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_TRANSACOES")
public class OpenFinanceTransacao extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFTR_ID")
    private Long id;

    @Column(name = "OFTR_ID_EXTERNO", length = 60, nullable = false, unique = true)
    private String idExterno;

    @Column(name = "OFTR_DESCRICAO", length = 255, nullable = false)
    private String descricao;

    @Column(name = "OFTR_VALOR", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "OFTR_DT_TRANSACAO", nullable = false)
    private LocalDate dataTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFTR_NATUREZA", length = 10, nullable = false)
    private NaturezaMovimento natureza;

    @Column(name = "OFTR_CATEGORIA_EXTERNA", length = 100)
    private String categoriaExterna;

    @Column(name = "OFTR_STATUS_EXTERNO", length = 20)
    private String statusExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFTR_STATUS_CONCILIACAO", length = 20, nullable = false)
    private StatusConciliacao statusConciliacao = StatusConciliacao.PENDENTE;

    @Column(name = "OFTR_TIPO_LANCAMENTO_GERADO", length = 20)
    private String tipoLancamentoGerado;

    @Column(name = "OFTR_ID_LANCAMENTO_GERADO")
    private Long idLancamentoGerado;

    @Column(name = "OFTR_DT_CONCILIACAO")
    private LocalDateTime dataConciliacao;

    @Column(name = "OFTR_DADOS_BRUTOS", columnDefinition = "json")
    private String dadosBrutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCE_ID", nullable = false)
    private OpenFinanceContaExterna contaExterna;
}
