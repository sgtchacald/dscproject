package br.com.diegocordeiro.dscproject.model.comum;

import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Campos comuns a todo lançamento financeiro do usuário (receita, despesa,
 * transação bancária). O nome da coluna de {@code competencia}/{@code valor}/
 * {@code dataLancamento}/{@code origem} muda por tabela — cada entidade concreta
 * sobrescreve com {@code @AttributeOverride}. {@code conta} e {@code categoria}
 * usam o mesmo nome de coluna (CTA_ID/CATE_ID) em todas as tabelas do domínio.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class LancamentoFinanceiro extends AbstractAuditoria {

    @Column(name = "COMPETENCIA", length = 7, nullable = false)
    private YearMonth competencia;

    @Column(name = "VALOR", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "DT_LANCAMENTO", nullable = false)
    private LocalDate dataLancamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORIGEM", length = 20, nullable = false)
    private OrigemLancamento origem = OrigemLancamento.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTA_ID")
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATE_ID")
    private Categoria categoria;
}
