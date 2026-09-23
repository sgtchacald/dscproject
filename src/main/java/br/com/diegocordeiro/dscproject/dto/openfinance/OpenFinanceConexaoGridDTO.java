package br.com.diegocordeiro.dscproject.dto.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.enums.StatusConsentimento;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OpenFinanceConexaoGridDTO {
    private Long id;
    private String idExterno;
    private StatusConexao status;
    private String statusDescricao;
    private String statusBadgeClass;
    private String statusIcone;
    private String statusDetalhe;
    private LocalDateTime ultimaSincronizacaoEm;
    private LocalDateTime proximaSincronizacaoEm;

    // Instituição
    private Long instituicaoId;
    private String instituicaoNome;
    private String instituicaoCodigo;
    private String instituicaoCorHex;

    // Provedor
    private Long provedorId;
    private String provedorNome;
    private String provedorCodigo;

    // Consentimento
    private StatusConsentimento consentimentoStatus;
    private LocalDateTime consentimentoExpiracao;
    private boolean consentimentoExpirando;
    private Long diasParaExpirar;

    // Contadores
    private int totalContas;
    private int totalContasVinculadas;
    private boolean podeSincronizar;
    private Long minutosParaLiberarSincronizacao;
}
