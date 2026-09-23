package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusSincronizacao;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
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

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_SINCRONIZACOES")
public class OpenFinanceSincronizacao extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFSI_ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFSI_TIPO", length = 20, nullable = false)
    private TipoSincronizacao tipo;

    @Column(name = "OFSI_INICIADO_EM", nullable = false)
    private LocalDateTime iniciadoEm = LocalDateTime.now();

    @Column(name = "OFSI_FINALIZADO_EM")
    private LocalDateTime finalizadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFSI_STATUS", length = 20, nullable = false)
    private StatusSincronizacao status = StatusSincronizacao.EXECUTANDO;

    @Column(name = "OFSI_QTD_TRANSACOES_NOVAS", nullable = false)
    private int qtdTransacoesNovas = 0;

    @Column(name = "OFSI_QTD_CONTAS_ATUALIZADAS", nullable = false)
    private int qtdContasAtualizadas = 0;

    @Column(name = "OFSI_ERRO_MENSAGEM", length = 500)
    private String erroMensagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCX_ID", nullable = false)
    private OpenFinanceConexao conexao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFEV_ID")
    private OpenFinanceEventoWebhook eventoOrigem;
}
