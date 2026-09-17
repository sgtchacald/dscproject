package br.com.diegocordeiro.dscproject.dto.transacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoImportacaoOFXDTO {

    private int totalLidos;
    private int totalImportados;
    private int totalIgnorados;
    private String mensagem;
}
