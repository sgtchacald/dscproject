package br.com.diegocordeiro.dscproject.dto.transacao;

import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import lombok.Getter;
import lombok.Setter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class TransacaoBancariaFiltroDTO {

    private String busca;
    private String competenciaInicio;
    private String competenciaFim;
    private Long contaId;
    private NaturezaMovimento natureza;
    private Long categoriaId;

    public YearMonth getCompetenciaInicioYearMonth() {
        return parseYearMonth(competenciaInicio);
    }

    public YearMonth getCompetenciaFimYearMonth() {
        return parseYearMonth(competenciaFim);
    }

    public boolean isCompetenciaUnica() {
        YearMonth ini = getCompetenciaInicioYearMonth();
        YearMonth fim = getCompetenciaFimYearMonth();
        return ini != null && ini.equals(fim);
    }

    private YearMonth parseYearMonth(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(valor.trim(), DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            return null;
        }
    }
}
