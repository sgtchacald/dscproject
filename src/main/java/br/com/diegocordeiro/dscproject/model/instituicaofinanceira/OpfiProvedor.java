package br.com.diegocordeiro.dscproject.model.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Provedor de integração Open Finance (ex.: Pluggy, Belvo).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_PROVEDORES")
public class OpfiProvedor extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFPV_ID")
    private Long id;

    @Column(name = "OFPV_CODIGO", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(name = "OFPV_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "OFPV_URL_BASE", length = 300)
    private String urlBase;

    @Column(name = "OFPV_FL_SUPORTA_WEBHOOK", nullable = false)
    private boolean suportaWebhook = false;

    @Column(name = "OFPV_PARAMETROS", columnDefinition = "json")
    private String parametros;

    @Column(name = "OFPV_FL_ATIVO", nullable = false)
    private boolean ativo = true;
}
