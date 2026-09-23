package br.com.diegocordeiro.dscproject.web.sistema.controller.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyWebhookPayloadDTO;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/open-finance/webhook")
public class OpenFinanceWebhookController {

    private final OpenFinanceWebhookService webhookService;

    public OpenFinanceWebhookController(OpenFinanceWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/{provedorCodigo}")
    public ResponseEntity<Map<String, Object>> receberWebhook(@PathVariable String provedorCodigo, @RequestBody PluggyWebhookPayloadDTO payload, @RequestHeader(value = "X-Pluggy-Signature", required = false) String signature) {
        webhookService.receberEvento(provedorCodigo, payload, signature);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Evento recebido com sucesso"));
    }
}
