package br.com.diegocordeiro.dscproject.dto.transacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoBancariaTotaisDTO {

    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private BigDecimal saldoLiquido;
    private boolean exibir;

    public static TransacaoBancariaTotaisDTO vazio() {
        return TransacaoBancariaTotaisDTO.builder()
            .totalCreditos(BigDecimal.ZERO)
            .totalDebitos(BigDecimal.ZERO)
            .saldoLiquido(BigDecimal.ZERO)
            .exibir(false)
            .build();
    }
}
