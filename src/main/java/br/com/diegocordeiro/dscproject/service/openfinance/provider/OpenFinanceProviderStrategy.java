package br.com.diegocordeiro.dscproject.service.openfinance.provider;

import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyAccountDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyBillDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyItemDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyTransactionDTO;
import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;

import java.time.LocalDate;
import java.util.List;

public interface OpenFinanceProviderStrategy {

    String getProvedorCodigo();

    boolean validarCredenciais(String clientId, String clientSecret, AmbienteCredencial ambiente);

    String gerarConnectToken(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemIdOpcional);

    PluggyItemDTO buscarItem(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemId);

    void revogarItem(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemId);

    List<PluggyAccountDTO> buscarContas(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemId);

    List<PluggyTransactionDTO> buscarTransacoes(
            String clientId,
            String clientSecret,
            AmbienteCredencial ambiente,
            String accountId,
            LocalDate de,
            LocalDate ate
    );

    List<PluggyBillDTO> buscarFaturas(String clientId, String clientSecret, AmbienteCredencial ambiente, String accountId);
}
