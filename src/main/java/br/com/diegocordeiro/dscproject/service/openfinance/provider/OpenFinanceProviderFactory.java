package br.com.diegocordeiro.dscproject.service.openfinance.provider;

import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OpenFinanceProviderFactory {

    private final Map<String, OpenFinanceProviderStrategy> strategies;

    public OpenFinanceProviderFactory(List<OpenFinanceProviderStrategy> providerStrategies) {
        this.strategies = providerStrategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getProvedorCodigo().toUpperCase(),
                        Function.identity()
                ));
    }

    public OpenFinanceProviderStrategy obterStrategy(String provedorCodigo) {
        if (provedorCodigo == null) {
            throw new RegraNegocioException("openfinance.provedor.codigo-obrigatorio");
        }
        OpenFinanceProviderStrategy strategy = strategies.get(provedorCodigo.toUpperCase());
        if (strategy == null) {
            throw new RegraNegocioException("openfinance.provedor.nao-suportado");
        }
        return strategy;
    }
}
