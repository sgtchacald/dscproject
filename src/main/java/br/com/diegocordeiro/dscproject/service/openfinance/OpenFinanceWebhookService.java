package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyWebhookPayloadDTO;
import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.enums.StatusEventoWebhook;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceEventoWebhook;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConexaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceEventoWebhookRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OpenFinanceWebhookService {

    private static final Logger log = LoggerFactory.getLogger(OpenFinanceWebhookService.class);

    private final OpenFinanceEventoWebhookRepository eventoWebhookRepository;
    private final OpenFinanceProvedorRepository provedorRepository;
    private final OpenFinanceConexaoRepository conexaoRepository;
    private final OpenFinanceSincronizacaoService sincronizacaoService;
    private final ObjectMapper objectMapper;

    @Value("${app.openfinance.webhook.verify-signature:false}")
    private boolean verifySignature = false;

    public OpenFinanceWebhookService(OpenFinanceEventoWebhookRepository eventoWebhookRepository, OpenFinanceProvedorRepository provedorRepository, OpenFinanceConexaoRepository conexaoRepository, OpenFinanceSincronizacaoService sincronizacaoService, ObjectMapper objectMapper) {
        this.eventoWebhookRepository = eventoWebhookRepository;
        this.provedorRepository = provedorRepository;
        this.conexaoRepository = conexaoRepository;
        this.sincronizacaoService = sincronizacaoService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void receberEvento(String provedorCodigo, PluggyWebhookPayloadDTO payload, String signature) {
        OpenFinanceProvedor provedor = provedorRepository.findByCodigo(provedorCodigo)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.provedor.nao-encontrado"));

        if (verifySignature && (signature == null || signature.isBlank())) {
            log.warn("Rejeitando webhook do provedor {}: assinatura ausente", provedorCodigo);
            throw new IllegalArgumentException("Assinatura do webhook inválida.");
        }

        String idEvento = payload.getId();
        if (idEvento != null && eventoWebhookRepository.existsByIdEventoExterno(idEvento)) {
            log.info("Evento de webhook {} ja processado (idempotencia garantida)", idEvento);
            return;
        }

        OpenFinanceEventoWebhook evento = new OpenFinanceEventoWebhook();
        evento.setProvedor(provedor);
        evento.setIdEventoExterno(idEvento);
        evento.setTipo(payload.getEvent() != null ? payload.getEvent() : "UNKNOWN");
        evento.setIdExterno(payload.getItemId());
        evento.setRecebidoEm(LocalDateTime.now());
        evento.setStatus(StatusEventoWebhook.PENDENTE);

        try {
            evento.setPayload(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            evento.setPayload("{}");
        }

        Optional<OpenFinanceConexao> optConexao = Optional.empty();
        if (payload.getItemId() != null) {
            optConexao = conexaoRepository.findByIdExternoAndProvedorIdAndDataExclusaoIsNull(payload.getItemId(), provedor.getId());
            optConexao.ifPresent(evento::setConexao);
        }

        evento = eventoWebhookRepository.save(evento);

        processarEventoAssincrono(evento.getId());
    }

    @Async
    @Transactional
    public void processarEventoAssincrono(Long eventoId) {
        Optional<OpenFinanceEventoWebhook> opt = eventoWebhookRepository.findById(eventoId);
        if (opt.isEmpty()) {
            return;
        }

        OpenFinanceEventoWebhook evento = opt.get();
        try {
            OpenFinanceConexao conexao = evento.getConexao();
            String tipo = evento.getTipo().toLowerCase();

            if (conexao != null) {
                if (tipo.contains("updated") || tipo.contains("created")) {
                    sincronizacaoService.sincronizarConexao(conexao.getId(), TipoSincronizacao.WEBHOOK, null);
                } else if (tipo.contains("error") || tipo.contains("login")) {
                    conexao.setStatus(StatusConexao.ERRO_LOGIN);
                    conexaoRepository.save(conexao);
                }
            }

            evento.setStatus(StatusEventoWebhook.PROCESSADO);
            evento.setProcessadoEm(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Erro ao processar evento webhook {}: {}", eventoId, e.getMessage(), e);
            evento.setStatus(StatusEventoWebhook.ERRO);
            evento.setErroMensagem(e.getMessage());
            evento.setTentativas(evento.getTentativas() + 1);
        }

        eventoWebhookRepository.save(evento);
    }
}
