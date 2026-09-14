package br.com.diegocordeiro.dscproject.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que todos os rótulos e textos dos modais de importação de fatura e
 * duplicação de despesas existem em labels.properties / messages.properties e
 * são resolvidos sem renderizar a chave crua '??chave_pt_BR??'.
 */
class MensagensDespesaImportacaoTest {

    private final MessageSource messageSource = new WebMvcConfig().messageSource();
    private final Locale locale = new Locale("pt", "BR");

    @Test
    @DisplayName("Geral - Chaves comuns devem resolver")
    void comum_chavesDevemResolver() {
        assertThat(messageSource.getMessage("comum.selecione", null, locale)).isEqualTo("Selecione...");
        assertThat(messageSource.getMessage("comum.botao.cancelar", null, locale)).isEqualTo("Cancelar");
    }

    @Test
    @DisplayName("Modal Importar Fatura - Todas as chaves devem resolver para os textos esperados")
    void importarFatura_chavesDevemResolver() {
        assertThat(messageSource.getMessage("despesa.importar.titulo", null, locale))
            .isEqualTo("Importar fatura de cartão de crédito");
        assertThat(messageSource.getMessage("despesa.importar.campo.cartao.label", null, locale))
            .isEqualTo("Cartão de crédito");
        assertThat(messageSource.getMessage("despesa.importar.campo.cartao.selecione", null, locale))
            .isEqualTo("Selecione o cartão...");
        assertThat(messageSource.getMessage("despesa.importar.campo.competencia.label", null, locale))
            .isEqualTo("Competência");
        assertThat(messageSource.getMessage("despesa.importar.campo.vencimento.label", null, locale))
            .isEqualTo("Vencimento da fatura");
        assertThat(messageSource.getMessage("despesa.importar.campo.formato.label", null, locale))
            .isEqualTo("Instituição / Formato");
        assertThat(messageSource.getMessage("despesa.importar.campo.formato.selecione", null, locale))
            .isEqualTo("Selecione o formato...");
        assertThat(messageSource.getMessage("despesa.importar.formato.itau", null, locale))
            .isEqualTo("Itaú (Excel .xls / .xlsx)");
        assertThat(messageSource.getMessage("despesa.importar.formato.bradesco", null, locale))
            .isEqualTo("Bradesco (Excel .xls / .xlsx)");
        assertThat(messageSource.getMessage("despesa.importar.formato.c6", null, locale))
            .isEqualTo("C6 Bank (Excel / CSV)");
        assertThat(messageSource.getMessage("despesa.importar.formato.ofx", null, locale))
            .isEqualTo("Extrato OFX / QFX");
        assertThat(messageSource.getMessage("despesa.importar.campo.categoriaPadrao.label", null, locale))
            .isEqualTo("Categoria padrão (opcional)");
        assertThat(messageSource.getMessage("despesa.importar.campo.categoriaPadrao.semCategoria", null, locale))
            .isEqualTo("Sem categoria");
        assertThat(messageSource.getMessage("despesa.importar.campo.arquivo.label", null, locale))
            .isEqualTo("Arquivo da fatura");
        assertThat(messageSource.getMessage("despesa.importar.campo.arquivo.ajuda", null, locale))
            .isEqualTo("Formatos suportados: Excel (.xls, .xlsx), CSV ou OFX.");
        assertThat(messageSource.getMessage("despesa.importar.botao.cancelar", null, locale))
            .isEqualTo("Cancelar");
        assertThat(messageSource.getMessage("despesa.importar.botao.importar", null, locale))
            .isEqualTo("Importar fatura");
    }

    @Test
    @DisplayName("Modal Duplicar Despesas - Todas as chaves devem resolver para os textos esperados")
    void duplicarDespesa_chavesDevemResolver() {
        assertThat(messageSource.getMessage("despesa.duplicar.titulo", null, locale))
            .isEqualTo("Duplicar despesa(s)");
        assertThat(messageSource.getMessage("despesa.duplicar.campo.competenciaDestino.label", null, locale))
            .isEqualTo("Competência de destino");
        assertThat(messageSource.getMessage("despesa.duplicar.campo.competenciaDestino.ajuda", null, locale))
            .isEqualTo("Competência (mês/ano) em que as novas despesas serão criadas.");
        assertThat(messageSource.getMessage("despesa.duplicar.botao.cancelar", null, locale))
            .isEqualTo("Cancelar");
        assertThat(messageSource.getMessage("despesa.duplicar.botao.duplicar", null, locale))
            .isEqualTo("Duplicar");
    }
}
