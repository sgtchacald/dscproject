package br.com.diegocordeiro.dscproject.dto.openfinance;

import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenFinanceCredencialDTO {

    @NotNull(message = "O provedor é obrigatório.")
    private Long provedorId;

    @NotBlank(message = "O Client ID é obrigatório.")
    private String clientId;

    private String clientSecret;

    @NotNull(message = "O ambiente é obrigatório.")
    private AmbienteCredencial ambiente = AmbienteCredencial.PRODUCTION;
}
