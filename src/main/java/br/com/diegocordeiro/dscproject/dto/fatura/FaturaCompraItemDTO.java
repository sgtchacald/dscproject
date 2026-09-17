package br.com.diegocordeiro.dscproject.dto.fatura;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturaCompraItemDTO {

    private Long despesaId;
    private LocalDate data;
    private String descricao;
    private String categoriaNome;
    private String parcela;
    private BigDecimal valor;
    private BigDecimal cotaTitular;
}
