package br.com.diegocordeiro.dscproject.dto.fatura;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturaPagamentoDTO {

    @NotNull(message = "A conta bancária de débito é obrigatória.")
    private Long contaId;

    @NotNull(message = "A data de pagamento é obrigatória.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPagamento;

    @NotNull(message = "O valor do pagamento é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero.")
    private BigDecimal valorPago;
}
