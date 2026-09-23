package br.com.diegocordeiro.dscproject.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MensagensOpenFinanceTest {

    private final MessageSource messageSource = new WebMvcConfig().messageSource();
    private final Locale locale = Locale.of("pt", "BR");

    @Test
    @DisplayName("MSG01 - Credenciais salvas com sucesso deve resolver")
    void msg01_credenciaisSalvas() {
        String msg = messageSource.getMessage("msg.openfinance.credenciais.salvas", null, locale);
        assertThat(msg).isEqualTo("Credenciais do provedor salvas e validadas com sucesso.");
    }

    @Test
    @DisplayName("MSG02 - Instituição conectada com sucesso deve resolver")
    void msg02_conexaoCriada() {
        String msg = messageSource.getMessage("msg.openfinance.conexao.criada", null, locale);
        assertThat(msg).isEqualTo("Instituição conectada com sucesso! A sincronização inicial foi iniciada em segundo plano.");
    }

    @Test
    @DisplayName("MSG03 - Sincronização iniciada com sucesso deve resolver")
    void msg03_sincronizacaoIniciada() {
        String msg = messageSource.getMessage("msg.openfinance.sincronizacao.iniciada", null, locale);
        assertThat(msg).isEqualTo("Sincronização iniciada com sucesso. Os dados serão atualizados em instantes.");
    }

    @Test
    @DisplayName("MSG04 - Conexão removida com sucesso deve resolver")
    void msg04_conexaoDesconectada() {
        String msg = messageSource.getMessage("msg.openfinance.conexao.desconectada", null, locale);
        assertThat(msg).isEqualTo("Conexão removida e consentimento revogado com sucesso.");
    }

    @Test
    @DisplayName("MSG05 - Vínculo da conta atualizado deve resolver")
    void msg05_vinculoAtualizado() {
        String msg = messageSource.getMessage("msg.openfinance.vinculo.atualizado", null, locale);
        assertThat(msg).isEqualTo("Vínculo da conta atualizado com sucesso.");
    }

    @Test
    @DisplayName("MSG06 - Não há credenciais ativas configuradas deve resolver")
    void msg06_credencialNaoConfigurada() {
        String msg = messageSource.getMessage("openfinance.credencial.nao-configurada", null, locale);
        assertThat(msg).isEqualTo("Não há credenciais ativas configuradas para o provedor de Open Finance. Configure suas credenciais antes de conectar.");
    }

    @Test
    @DisplayName("MSG07 - Rate limit de sincronização deve resolver com placeholder")
    void msg07_syncRateLimit() {
        String msg = messageSource.getMessage("openfinance.sync.rate-limit", new Object[]{10}, locale);
        assertThat(msg).isEqualTo("Esta conexão foi sincronizada recentemente. Aguarde 10 minuto(s) para sincronizar novamente.");
    }

    @Test
    @DisplayName("MSG09 - Confirmação de desconexão deve resolver")
    void msg09_confirmaDesconectar() {
        String msg = messageSource.getMessage("openfinance.conexao.confirma-desconectar", null, locale);
        assertThat(msg).isEqualTo("Tem certeza de que deseja desconectar esta instituição? O consentimento será revogado junto ao banco.");
    }

    @Test
    @DisplayName("MSG10 - Conexão não encontrada ou acesso não autorizado deve resolver")
    void msg10_conexaoNaoEncontrada() {
        String msg = messageSource.getMessage("openfinance.conexao.nao-encontrada", null, locale);
        assertThat(msg).isEqualTo("Conexão não encontrada ou acesso não autorizado.");
    }

    @Test
    @DisplayName("MSG11 - Falha na validação das credenciais deve resolver")
    void msg11_credencialInvalida() {
        String msg = messageSource.getMessage("openfinance.credencial.invalida", null, locale);
        assertThat(msg).isEqualTo("Falha na validação das credenciais junto ao provedor. Verifique o Client ID e Client Secret informados.");
    }

    @Test
    @DisplayName("MSG13 - Incompatibilidade de tipo de conta deve resolver")
    void msg13_vinculoTipoIncompativel() {
        String msg = messageSource.getMessage("openfinance.vinculo.tipo-incompativel", null, locale);
        assertThat(msg).isEqualTo("O tipo da conta externa não é compatível com o registro selecionado para vínculo.");
    }

    @Test
    @DisplayName("Labels da tela Open Finance devem resolver corretamente")
    void labels_devemResolver() {
        assertThat(messageSource.getMessage("openfinance.titulo", null, locale)).isEqualTo("Conexões Open Finance");
        assertThat(messageSource.getMessage("openfinance.btn.novaConexao", null, locale)).isEqualTo("Nova Conexão");
        assertThat(messageSource.getMessage("openfinance.btn.credenciais", null, locale)).isEqualTo("Configurar Credenciais");
    }
}
