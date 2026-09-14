package br.com.diegocordeiro.dscproject.model.categoria;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
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
 * Mapeamento entre rótulos de categoria fornecidos por serviços externos e as categorias do sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "CATEGORIAS_PROVEDOR")
public class CategoriaProvedor extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CAPR_ID")
    private Long id;

    @Column(name = "CAPR_ROTULO_EXTERNO", length = 120, nullable = false)
    private String rotuloExterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATE_ID", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFPV_ID", nullable = false)
    private OpfiProvedor provedor;
}
