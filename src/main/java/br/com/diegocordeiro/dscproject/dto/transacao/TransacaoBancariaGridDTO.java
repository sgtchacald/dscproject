package br.com.diegocordeiro.dscproject.dto.transacao;

import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
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
public class TransacaoBancariaGridDTO {

    private Long id;
    private YearMonth competencia;
    private String descricao;
    private BigDecimal valor;
    private NaturezaMovimento natureza;
    private LocalDate dataLancamento;
    private boolean pagamentoFatura;
    private String idExterno;
    private OrigemLancamento origem;
    private Long contaId;
    private String contaDescricao;
    private Long categoriaId;
    private String categoriaNome;
    private boolean excluido;
}
