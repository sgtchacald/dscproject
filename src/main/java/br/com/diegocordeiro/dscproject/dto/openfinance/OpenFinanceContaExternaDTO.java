package br.com.diegocordeiro.dscproject.dto.openfinance;

import br.com.diegocordeiro.dscproject.enums.TipoContaExterna;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OpenFinanceContaExternaDTO {
    private Long id;
    private String idExterno;
    private TipoContaExterna tipo;
    private String tipoDescricao;
    private String subtipo;
    private String nome;
    private String numero;
    private BigDecimal saldo;
    private LocalDateTime saldoEm;
    private String moeda;

    // Vínculos do domínio
    private Long contaId;
    private String contaDescricao;
    private Long cartaoId;
    private String cartaoDescricao;
}
