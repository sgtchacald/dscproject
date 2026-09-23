package br.com.diegocordeiro.dscproject.dto.openfinance;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenFinanceCallbackConexaoDTO {

    @NotBlank(message = "O ID da conexão externa (Item ID) é obrigatório.")
    private String itemId;
}
