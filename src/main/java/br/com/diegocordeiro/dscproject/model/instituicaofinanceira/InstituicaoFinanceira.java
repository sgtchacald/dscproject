package br.com.diegocordeiro.dscproject.model.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
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
 * Catálogo global de bancos e corretoras.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "INSTITUICOES_FINANCEIRAS")
public class InstituicaoFinanceira extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFI_ID")
    private Long id;

    @Column(name = "INFI_NOME", length = 100, nullable = false, unique = true)
    private String nome;

    @Column(name = "INFI_CODIGO", length = 100, unique = true)
    private String codigo;

    @Column(name = "INFI_TIPO_INSTITUICAO", length = 1, nullable = false)
    private TipoInstituicaoFinanceira tipo;

    @Column(name = "INFI_FL_ATIVO", nullable = false)
    private boolean ativo = true;

    @Column(name = "INFI_FL_SISTEMA", nullable = false)
    private boolean sistema = false;
}
