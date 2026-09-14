package br.com.diegocordeiro.dscproject.model.perfil;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Vínculo N:N entre perfil e permissão.
 * Entidade própria (PK e auditoria) porque a tela {@code manter-perfil-permissao}
 * liga/desliga estes vínculos e o histórico é auditado.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(
    name = "PERFIL_PERMISSAO",
    uniqueConstraints = @UniqueConstraint(name = "uq_perfil_permissao_par", columnNames = {"PERF_ID", "PERM_ID"})
)
public class PerfilPermissao extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PEPE_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "PERF_ID", nullable = false)
    private Perfil perfil;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "PERM_ID", nullable = false)
    private Permissao permissao;

    public PerfilPermissao(Perfil perfil, Permissao permissao) {
        this.perfil = perfil;
        this.permissao = permissao;
    }
}
