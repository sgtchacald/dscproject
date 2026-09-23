package br.com.diegocordeiro.dscproject.service.openfinance.provider;

import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyAccountDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyAuthRequestDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyAuthResponseDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyBillDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyConnectTokenRequestDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyConnectTokenResponseDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyItemDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyPageResponseDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyTransactionDTO;
import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PluggyProviderStrategy implements OpenFinanceProviderStrategy {

    private static final Logger log = LoggerFactory.getLogger(PluggyProviderStrategy.class);
    private static final String PROVEDOR_CODIGO = "PLUGGY";
    private static final String HEADER_API_KEY = "X-API-KEY";

    private final RestClient restClient;

    @Autowired
    public PluggyProviderStrategy(@Value("${app.openfinance.pluggy.base-url:https://api.pluggy.ai}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PluggyProviderStrategy(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String getProvedorCodigo() {
        return PROVEDOR_CODIGO;
    }

    @Override
    public boolean validarCredenciais(String clientId, String clientSecret, AmbienteCredencial ambiente) {
        try {
            String apiKey = autenticar(clientId, clientSecret);
            return apiKey != null && !apiKey.isBlank();
        } catch (Exception e) {
            log.warn("Falha na validacao de credenciais Pluggy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String gerarConnectToken(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemIdOpcional) {
        String apiKey = autenticar(clientId, clientSecret);
        PluggyConnectTokenRequestDTO body = new PluggyConnectTokenRequestDTO(itemIdOpcional);
        PluggyConnectTokenResponseDTO resp = restClient.post()
                .uri("/connect_token")
                .header(HEADER_API_KEY, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(PluggyConnectTokenResponseDTO.class);
        if (resp == null || resp.getAccessToken() == null) {
            throw new RegraNegocioException("openfinance.connect-token.falha");
        }
        return resp.getAccessToken();
    }

    @Override
    public PluggyItemDTO buscarItem(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemId) {
        String apiKey = autenticar(clientId, clientSecret);
        return restClient.get()
                .uri("/items/{id}", itemId)
                .header(HEADER_API_KEY, apiKey)
                .retrieve()
                .body(PluggyItemDTO.class);
    }

    @Override
    public void revogarItem(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemId) {
        String apiKey = autenticar(clientId, clientSecret);
        restClient.delete()
                .uri("/items/{id}", itemId)
                .header(HEADER_API_KEY, apiKey)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<PluggyAccountDTO> buscarContas(String clientId, String clientSecret, AmbienteCredencial ambiente, String itemId) {
        String apiKey = autenticar(clientId, clientSecret);
        PluggyPageResponseDTO<PluggyAccountDTO> resp = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/accounts")
                        .queryParam("itemId", itemId)
                        .build())
                .header(HEADER_API_KEY, apiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<PluggyPageResponseDTO<PluggyAccountDTO>>() {});
        return (resp != null && resp.getResults() != null) ? resp.getResults() : Collections.emptyList();
    }

    @Override
    public List<PluggyTransactionDTO> buscarTransacoes(
            String clientId,
            String clientSecret,
            AmbienteCredencial ambiente,
            String accountId,
            LocalDate de,
            LocalDate ate
    ) {
        String apiKey = autenticar(clientId, clientSecret);
        int page = 1;
        int totalPages = 1;
        List<PluggyTransactionDTO> todas = new ArrayList<>();
        do {
            int currentPage = page;
            PluggyPageResponseDTO<PluggyTransactionDTO> resp = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/transactions")
                                .queryParam("accountId", accountId)
                                .queryParam("page", currentPage);
                        if (de != null) {
                            builder.queryParam("from", de.toString());
                        }
                        if (ate != null) {
                            builder.queryParam("to", ate.toString());
                        }
                        return builder.build();
                    })
                    .header(HEADER_API_KEY, apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PluggyPageResponseDTO<PluggyTransactionDTO>>() {});
            if (resp != null && resp.getResults() != null) {
                todas.addAll(resp.getResults());
                totalPages = resp.getTotalPages();
            } else {
                break;
            }
            page++;
        } while (page <= totalPages && page <= 50);

        return todas;
    }

    @Override
    public List<PluggyBillDTO> buscarFaturas(String clientId, String clientSecret, AmbienteCredencial ambiente, String accountId) {
        String apiKey = autenticar(clientId, clientSecret);
        PluggyPageResponseDTO<PluggyBillDTO> resp = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/bills")
                        .queryParam("accountId", accountId)
                        .build())
                .header(HEADER_API_KEY, apiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<PluggyPageResponseDTO<PluggyBillDTO>>() {});
        return (resp != null && resp.getResults() != null) ? resp.getResults() : Collections.emptyList();
    }

    private String autenticar(String clientId, String clientSecret) {
        PluggyAuthRequestDTO req = new PluggyAuthRequestDTO(clientId, clientSecret);
        PluggyAuthResponseDTO resp = restClient.post()
                .uri("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(PluggyAuthResponseDTO.class);
        if (resp == null || resp.getApiKey() == null) {
            throw new RegraNegocioException("openfinance.auth.falha");
        }
        return resp.getApiKey();
    }
}
