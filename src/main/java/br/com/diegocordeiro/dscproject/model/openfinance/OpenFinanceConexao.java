package br.com.diegocordeiro.dscproject.model.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "OPFI_CONEXOES")
public class OpenFinanceConexao extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OFCX_ID")
    private Long id;

    @Column(name = "OFCX_ID_EXTERNO", length = 60, nullable = false)
    private String idExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFCX_STATUS", length = 30, nullable = false)
    private StatusConexao status = StatusConexao.ATUALIZANDO;

    @Column(name = "OFCX_STATUS_DETALHE", length = 200)
    private String statusDetalhe;

    @Column(name = "OFCX_EXECUCAO_STATUS", length = 40)
    private String execucaoStatus;

    @Column(name = "OFCX_ULTIMA_SINCRONIZACAO_EM")
    private LocalDateTime ultimaSincronizacaoEm;

    @Column(name = "OFCX_PROXIMA_SINCRONIZACAO_EM")
    private LocalDateTime proximaSincronizacaoEm;

    @Column(name = "OFCX_ERRO_CODIGO", length = 60)
    private String erroCodigo;

    @Column(name = "OFCX_ERRO_MENSAGEM", length = 500)
    private String erroMensagem;

    @Column(name = "OFCX_PARAMETROS", columnDefinition = "json")
    private String parametros;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFI_ID")
    private InstituicaoFinanceira instituicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFPV_ID", nullable = false)
    private OpenFinanceProvedor provedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFCR_ID", nullable = false)
    private OpenFinanceCredencial credencial;

    @OneToOne(mappedBy = "conexao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OpenFinanceConsentimento consentimento;

    @OneToMany(mappedBy = "conexao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OpenFinanceContaExterna> contasExternas = new ArrayList<>();
}
