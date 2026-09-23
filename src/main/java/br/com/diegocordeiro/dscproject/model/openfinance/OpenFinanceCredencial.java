package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
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

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_CREDENCIAIS")
public class OpenFinanceCredencial extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFCR_ID")
    private Long id;

    @Column(name = "OFCR_CLIENT_ID", length = 200, nullable = false)
    private String clientId;

    @Column(name = "OFCR_CLIENT_SECRET", columnDefinition = "TEXT", nullable = false)
    private String clientSecret;

    @Column(name = "OFCR_SECRET_ATUALIZADO_EM")
    private LocalDateTime secretAtualizadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFCR_AMBIENTE", length = 20, nullable = false)
    private AmbienteCredencial ambiente = AmbienteCredencial.PRODUCTION;

    @Column(name = "OFCR_FL_ATIVO", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFPV_ID", nullable = false)
    private OpenFinanceProvedor provedor;
}
