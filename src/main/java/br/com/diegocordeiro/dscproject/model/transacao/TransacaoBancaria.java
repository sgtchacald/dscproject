package br.com.diegocordeiro.dscproject.model.transacao;

import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.model.comum.LancamentoFinanceiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Movimentação bancária (crédito ou débito) em conta do usuário.
 * Dono indireto via conta.usuario — TRANSACOES_BANCARIAS não tem USU_ID próprio.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "TRANSACOES_BANCARIAS")
@AttributeOverrides({
    @AttributeOverride(name = "competencia", column = @Column(name = "TRBA_COMPETENCIA", length = 7, nullable = false)),
    @AttributeOverride(name = "valor", column = @Column(name = "TRBA_VALOR", precision = 15, scale = 2, nullable = false)),
    @AttributeOverride(name = "dataLancamento", column = @Column(name = "TRBA_DT_LANCAMENTO", nullable = false)),
    @AttributeOverride(name = "origem", column = @Column(name = "TRBA_ORIGEM", length = 20, nullable = false))
})
public class TransacaoBancaria extends LancamentoFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRBA_ID")
    private Long id;

    @Column(name = "TRBA_DESCRICAO", length = 512, nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRBA_NATUREZA_MOVIMENTO", length = 10, nullable = false)
    private NaturezaMovimento natureza;

    @Column(name = "TRBA_FL_PAGAMENTO_FATURA", nullable = false)
    private boolean pagamentoFatura = false;

    @Column(name = "TRBA_ID_EXTERNO", length = 120, unique = true)
    private String idExterno;
}
