package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusEventoWebhook;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
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
@Table(name = "OPFI_EVENTOS_WEBHOOK")
public class OpenFinanceEventoWebhook extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFEV_ID")
    private Long id;

    @Column(name = "OFEV_ID_EVENTO_EXTERNO", length = 80)
    private String idEventoExterno;

    @Column(name = "OFEV_TIPO", length = 60, nullable = false)
    private String tipo;

    @Column(name = "OFEV_ID_EXTERNO", length = 60)
    private String idExterno;

    @Column(name = "OFEV_PAYLOAD", columnDefinition = "json", nullable = false)
    private String payload;

    @Column(name = "OFEV_RECEBIDO_EM", nullable = false)
    private LocalDateTime recebidoEm = LocalDateTime.now();

    @Column(name = "OFEV_PROCESSADO_EM")
    private LocalDateTime processadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFEV_STATUS", length = 20, nullable = false)
    private StatusEventoWebhook status = StatusEventoWebhook.PENDENTE;

    @Column(name = "OFEV_ERRO_MENSAGEM", length = 500)
    private String erroMensagem;

    @Column(name = "OFEV_TENTATIVAS", nullable = false)
    private int tentativas = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFPV_ID", nullable = false)
    private OpenFinanceProvedor provedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCX_ID")
    private OpenFinanceConexao conexao;
}
