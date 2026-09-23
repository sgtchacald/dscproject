package br.com.diegocordeiro.dscproject.dto.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusSincronizacao;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OpenFinanceSincronizacaoDTO {
    private Long id;
    private TipoSincronizacao tipo;
    private String tipoDescricao;
    private LocalDateTime iniciadoEm;
    private LocalDateTime finalizadoEm;
    private StatusSincronizacao status;
    private String statusDescricao;
    private int qtdTransacoesNovas;
    private int qtdContasAtualizadas;
    private String erroMensagem;
}
