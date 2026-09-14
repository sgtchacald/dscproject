package br.com.diegocordeiro.dscproject.model.conta;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Conta bancária, poupança, investimento ou carteira do próprio usuário.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "CONTAS")
public class Conta extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CTA_ID")
    private Long id;

    @Column(name = "CTA_DESCRICAO", length = 100, nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "CTA_TIPO", length = 20, nullable = false)
    private TipoConta tipo;

    @Column(name = "CTA_AGENCIA", length = 30)
    private String agencia;

    @Column(name = "CTA_NUMERO", length = 30)
    private String numero;

    @Column(name = "CTA_MOEDA", length = 3, nullable = false)
    private String moeda = "BRL";

    @Column(name = "CTA_SALDO", precision = 15, scale = 2, nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "CTA_SALDO_SINCRONIZADO_EM")
    private Instant saldoSincronizadoEm;

    @Column(name = "CTA_NOME_GERENTE", length = 100)
    private String nomeGerente;

    @Column(name = "CTA_TEL_GERENTE", length = 20)
    private String telGerente;

    @Column(name = "CTA_FL_ATIVO", nullable = false)
    private boolean ativo = true;

    @Column(name = "CTA_FL_CONSIDERA_SALDO", nullable = false)
    private boolean consideraSaldo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFI_ID", nullable = false)
    private InstituicaoFinanceira instituicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;
}
