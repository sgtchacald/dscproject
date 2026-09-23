package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.TipoContaExterna;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_CONTAS_EXTERNAS")
public class OpenFinanceContaExterna extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFCE_ID")
    private Long id;

    @Column(name = "OFCE_ID_EXTERNO", length = 60, nullable = false, unique = true)
    private String idExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFCE_TIPO", length = 20, nullable = false)
    private TipoContaExterna tipo;

    @Column(name = "OFCE_SUBTIPO", length = 40)
    private String subtipo;

    @Column(name = "OFCE_NOME", length = 150)
    private String nome;

    @Column(name = "OFCE_NUMERO", length = 40)
    private String numero;

    @Column(name = "OFCE_SALDO", precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(name = "OFCE_SALDO_EM")
    private LocalDateTime saldoEm;

    @Column(name = "OFCE_MOEDA", length = 3, nullable = false)
    private String moeda = "BRL";

    @Column(name = "OFCE_DADOS_BRUTOS", columnDefinition = "json")
    private String dadosBrutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCX_ID", nullable = false)
    private OpenFinanceConexao conexao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTA_ID")
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CACR_ID")
    private CartaoCredito cartaoCredito;
}
