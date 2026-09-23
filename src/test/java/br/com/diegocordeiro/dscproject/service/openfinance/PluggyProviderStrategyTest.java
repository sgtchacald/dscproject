package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.PluggyProviderStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PluggyProviderStrategyTest {

    private final PluggyProviderStrategy strategy = new PluggyProviderStrategy("http://localhost:9999");

    @Test
    @DisplayName("Código do provedor retornado deve ser 'PLUGGY'")
    void getProvedorCodigo_retornaPluggy() {
        assertThat(strategy.getProvedorCodigo()).isEqualTo("PLUGGY");
    }

    @Test
    @DisplayName("Validar credenciais com servidor indisponível deve capturar exceção e retornar false")
    void validarCredenciais_servidorIndisponivel_retornaFalse() {
        boolean valida = strategy.validarCredenciais("id-teste", "secret-teste", AmbienteCredencial.SANDBOX);
        assertThat(valida).isFalse();
    }
}
