package br.com.diegocordeiro.dscproject.model.cartao;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
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

import java.math.BigDecimal;

/**
 * Cartão de crédito do próprio usuário. Guarda só o necessário para o cadastro
 * e para os dois dias que a geração da fatura consome — nunca o número completo.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "CARTOES_CREDITO")
public class CartaoCredito extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CACR_ID")
    private Long id;

    @Column(name = "CACR_DESCRICAO", length = 100, nullable = false)
    private String descricao;

    @Column(name = "CACR_BANDEIRA", length = 30)
    private String bandeira;

    @Column(name = "CACR_FINAL_CARTAO", length = 4)
    private String finalCartao;

    @Column(name = "CACR_LIMITE", precision = 15, scale = 2)
    private BigDecimal limite;

    @Column(name = "CACR_DIA_FECHAMENTO")
    private Integer diaFechamento;

    @Column(name = "CACR_DIA_VENCIMENTO")
    private Integer diaVencimento;

    @Column(name = "CACR_FL_ATIVO", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTA_ID")
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;
}
