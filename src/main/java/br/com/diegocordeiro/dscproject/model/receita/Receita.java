package br.com.diegocordeiro.dscproject.model.receita;

import br.com.diegocordeiro.dscproject.model.comum.LancamentoFinanceiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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

import java.time.LocalDate;

/**
 * Entrada de dinheiro (prevista ou já recebida) do usuário. O dono é indireto,
 * via {@code conta.usuario} — {@code RECEITAS} não tem {@code USU_ID} próprio.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "RECEITAS")
@AttributeOverrides({
    @AttributeOverride(name = "competencia", column = @Column(name = "RECE_COMPETENCIA", length = 7, nullable = false)),
    @AttributeOverride(name = "valor", column = @Column(name = "RECE_VALOR", precision = 15, scale = 2, nullable = false)),
    @AttributeOverride(name = "dataLancamento", column = @Column(name = "RECE_DT_LANCAMENTO", nullable = false)),
    @AttributeOverride(name = "origem", column = @Column(name = "RECE_ORIGEM", length = 20, nullable = false))
})
public class Receita extends LancamentoFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECE_ID")
    private Long id;

    @Column(name = "RECE_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "RECE_DESCRICAO", length = 512)
    private String descricao;

    @Column(name = "RECE_DT_RECEBIMENTO")
    private LocalDate dataRecebimento;

    @Column(name = "RECE_FL_RECEBIDO", nullable = false)
    private boolean recebido = false;
}
