package br.com.diegocordeiro.dscproject.model.despesa;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
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
@Table(name = "DESPESAS_USUARIO")
public class DespesaUsuario extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPU_ID")
    private Long id;

    @Column(name = "DEPU_VALOR", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "DEPU_IND_STATUS_PAGAMENTO", length = 20, nullable = false)
    private StatusPagamento statusPagamento = StatusPagamento.NAO;

    @Column(name = "DEPU_DT_ACERTO")
    private LocalDate dataAcerto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESP_ID", nullable = false)
    private Despesa despesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONT_ID", nullable = false)
    private Contato contato;
}
