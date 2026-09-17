package br.com.diegocordeiro.dscproject.dto.fatura;

import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusFatura;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturaCartaoGridDTO {

    private Long id;
    private Long cartaoId;
    private String cartaoDescricao;
    private YearMonth competencia;
    private LocalDate dataFechamento;
    private LocalDate dataVencimento;
    private BigDecimal totalCompras;
    private BigDecimal valorEncargos;
    private BigDecimal valorTotal;
    private BigDecimal valorMinimo;
    private BigDecimal valorPago;
    private StatusFatura status;
    private OrigemLancamento origem;
    private Long transacaoPagamentoId;
}
