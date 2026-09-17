package br.com.diegocordeiro.dscproject.dto.fatura;

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
public class FaturaEncargosDTO {

    private BigDecimal valorEncargos;
    private BigDecimal valorMinimo;
}
