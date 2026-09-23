package br.com.diegocordeiro.dscproject.dto.openfinance;

import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OpenFinanceCredencialExibicaoDTO {
    private Long id;
    private Long provedorId;
    private String provedorNome;
    private String provedorCodigo;
    private String clientId;
    private AmbienteCredencial ambiente;
    private boolean ativo;
    private boolean possuiSecret;
    private String secretMascarado;
}
