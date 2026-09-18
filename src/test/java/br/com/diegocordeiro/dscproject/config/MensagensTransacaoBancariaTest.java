package br.com.diegocordeiro.dscproject.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que todos os rótulos e mensagens de transações bancárias existem em
 * labels.properties / messages.properties e são resolvidos sem renderizar a
 * chave crua '??chave_pt_BR??'.
 */
class MensagensTransacaoBancariaTest {

    private final MessageSource messageSource = new WebMvcConfig().messageSource();
    private final Locale locale = new Locale("pt", "BR");

    @Test
    @DisplayName("MSG08 - msg.transacao.filtro.vazio deve resolver")
    void filtroVazio_deveResolver() {
        String msg = messageSource.getMessage("msg.transacao.filtro.vazio", null, locale);
        assertThat(msg).isEqualTo("Nenhuma transação encontrada para os filtros selecionados.");
    }

    @Test
    @DisplayName("MSG06 - msg.transacao.confirma.exclusao deve resolver")
    void confirmaExclusao_deveResolver() {
        String msg = messageSource.getMessage("msg.transacao.confirma.exclusao", null, locale);
        assertThat(msg).isEqualTo("Deseja realmente excluir esta transação bancária?");
    }

    @Test
    @DisplayName("MSG01 e MSG04 - Cadastro e Atualização devem resolver")
    void cadastroEAtualizacao_devemResolver() {
        assertThat(messageSource.getMessage("msg.transacao.cadastrada", null, locale))
            .isEqualTo("Transação bancária cadastrada com sucesso.");
        assertThat(messageSource.getMessage("msg.transacao.atualizada", null, locale))
            .isEqualTo("Transação bancária atualizada com sucesso.");
    }

    @Test
    @DisplayName("MSG17 - Importação de OFX deve resolver com parâmetros")
    void importacaoOfx_deveResolverComParametros() {
        String msg = messageSource.getMessage("msg.transacao.importar.sucesso", new Object[]{5, 2}, locale);
        assertThat(msg).isEqualTo("Importação concluída com sucesso! 5 transações importadas, 2 ignoradas por duplicidade.");
    }

    @Test
    @DisplayName("Labels de listagem e totalizadores devem resolver")
    void labelsListagem_devemResolver() {
        assertThat(messageSource.getMessage("transacao.lista.titulo", null, locale)).isEqualTo("Transações Bancárias");
        assertThat(messageSource.getMessage("transacao.totalizador.creditos", null, locale)).isEqualTo("Créditos (Entradas)");
        assertThat(messageSource.getMessage("transacao.totalizador.debitos", null, locale)).isEqualTo("Débitos (Saídas)");
        assertThat(messageSource.getMessage("transacao.totalizador.liquido", null, locale)).isEqualTo("Saldo Líquido");
        assertThat(messageSource.getMessage("menu.transacoes-bancarias", null, locale)).isEqualTo("Transações Bancárias");
    }
}
