package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusConsentimento;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_CONSENTIMENTOS")
public class OpenFinanceConsentimento extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFCS_ID")
    private Long id;

    @Column(name = "OFCS_ID_EXTERNO", length = 60)
    private String idExterno;

    @Column(name = "OFCS_ESCOPOS", length = 255, nullable = false)
    private String escopos;

    @Column(name = "OFCS_DT_CONCESSAO", nullable = false)
    private LocalDateTime dataConcessao;

    @Column(name = "OFCS_DT_EXPIRACAO")
    private LocalDateTime dataExpiracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFCS_STATUS", length = 20, nullable = false)
    private StatusConsentimento status = StatusConsentimento.ATIVO;

    @Column(name = "OFCS_DT_REVOGACAO")
    private LocalDateTime dataRevogacao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCX_ID", nullable = false)
    private OpenFinanceConexao conexao;
}
