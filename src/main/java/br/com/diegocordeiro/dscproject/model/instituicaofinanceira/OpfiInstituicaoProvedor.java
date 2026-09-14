package br.com.diegocordeiro.dscproject.model.instituicaofinanceira;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Mapeamento entre a instituição financeira do sistema e o identificador do conector
 * retornado pelo provedor de Open Finance.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_INSTITUICAO_PROVEDOR")
public class OpfiInstituicaoProvedor extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFIP_ID")
    private Long id;

    @Column(name = "OFIP_ID_EXTERNO", length = 80, nullable = false)
    private String idExterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFI_ID", nullable = false)
    private InstituicaoFinanceira instituicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFPV_ID", nullable = false)
    private OpfiProvedor provedor;
}
