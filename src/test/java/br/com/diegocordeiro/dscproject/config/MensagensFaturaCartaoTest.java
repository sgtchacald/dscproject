package br.com.diegocordeiro.dscproject.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que todos os rótulos e mensagens de faturas de cartão existem em
 * labels.properties / messages.properties e são resolvidos sem renderizar a
 * chave crua '??chave_pt_BR??'.
 */
class MensagensFaturaCartaoTest {

    private final MessageSource messageSource = new WebMvcConfig().messageSource();
    private final Locale locale = new Locale("pt", "BR");

    @Test
    @DisplayName("Tabela Principal - Todas as colunas devem resolver")
    void colunasTabela_devemResolver() {
        assertThat(messageSource.getMessage("fatura.col.competencia", null, locale)).isEqualTo("Competência");
        assertThat(messageSource.getMessage("fatura.col.vencimento", null, locale)).isEqualTo("Vencimento");
        assertThat(messageSource.getMessage("fatura.col.fechamento", null, locale)).isEqualTo("Fechamento");
        assertThat(messageSource.getMessage("fatura.col.valorCompras", null, locale)).isEqualTo("Compras");
        assertThat(messageSource.getMessage("fatura.col.encargos", null, locale)).isEqualTo("Encargos");
        assertThat(messageSource.getMessage("fatura.col.valorTotal", null, locale)).isEqualTo("Total");
        assertThat(messageSource.getMessage("fatura.col.valorPago", null, locale)).isEqualTo("Valor Pago");
        assertThat(messageSource.getMessage("fatura.col.status", null, locale)).isEqualTo("Status");
        assertThat(messageSource.getMessage("fatura.col.acoes", null, locale)).isEqualTo("Ações");
    }

    @Test
    @DisplayName("Status da Fatura - Todas as legendas devem resolver")
    void statusFatura_devemResolver() {
        assertThat(messageSource.getMessage("fatura.status.aberta", null, locale)).isEqualTo("Aberta");
        assertThat(messageSource.getMessage("fatura.status.fechada", null, locale)).isEqualTo("Fechada");
        assertThat(messageSource.getMessage("fatura.status.paga", null, locale)).isEqualTo("Paga");
        assertThat(messageSource.getMessage("fatura.status.pagaParcial", null, locale)).isEqualTo("Paga Parcial");
    }

    @Test
    @DisplayName("Mensagens de Ciclo de Vida e Validações devem resolver")
    void mensagensCicloDeVida_devemResolver() {
        assertThat(messageSource.getMessage("msg.fatura.fechada.sucesso", null, locale))
            .isEqualTo("Fatura fechada com sucesso.");
        assertThat(messageSource.getMessage("msg.fatura.reaberta.sucesso", null, locale))
            .isEqualTo("Fatura reaberta com sucesso.");
        assertThat(messageSource.getMessage("msg.fatura.paga.sucesso", null, locale))
            .isEqualTo("Pagamento da fatura registrado com sucesso.");
        assertThat(messageSource.getMessage("msg.fatura.encargos.sucesso", null, locale))
            .isEqualTo("Encargos da fatura atualizados com sucesso.");
        assertThat(messageSource.getMessage("msg.fatura.filtro.vazio", null, locale))
            .isEqualTo("Nenhuma fatura encontrada para os filtros selecionados.");
    }
}
