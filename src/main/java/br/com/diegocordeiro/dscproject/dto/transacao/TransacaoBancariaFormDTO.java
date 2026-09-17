package br.com.diegocordeiro.dscproject.dto.transacao;

import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoBancariaFormDTO {

    private Long id;

    @NotBlank(message = "{transacao.validacao.descricao.obrigatoria}")
    @Size(max = 512, message = "{transacao.validacao.descricao.tamanho}")
    private String descricao;

    @NotNull(message = "{transacao.validacao.valor.obrigatorio}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{transacao.validacao.valor.positivo}")
    private BigDecimal valor;

    @NotNull(message = "{transacao.validacao.natureza.obrigatoria}")
    private NaturezaMovimento natureza;

    @NotNull(message = "{transacao.validacao.dataLancamento.obrigatoria}")
    private LocalDate dataLancamento;

    @NotBlank(message = "{transacao.validacao.competencia.obrigatoria}")
    @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$", message = "{transacao.validacao.competencia.formato}")
    private String competencia;

    @NotNull(message = "{transacao.validacao.contaId.obrigatoria}")
    private Long contaId;

    private Long categoriaId;
}
