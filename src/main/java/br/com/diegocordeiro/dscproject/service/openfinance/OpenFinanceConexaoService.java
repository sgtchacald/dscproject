package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceConexaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceContaExternaDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyItemDTO;
import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.enums.StatusConsentimento;
import br.com.diegocordeiro.dscproject.enums.TipoContaExterna;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConsentimento;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceContaExterna;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConexaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConsentimentoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceContaExternaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderFactory;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpenFinanceConexaoService {

    private static final Logger log = LoggerFactory.getLogger(OpenFinanceConexaoService.class);

    private final OpenFinanceConexaoRepository conexaoRepository;
    private final OpenFinanceConsentimentoRepository consentimentoRepository;
    private final OpenFinanceContaExternaRepository contaExternaRepository;
    private final OpenFinanceInstituicaoProvedorRepository instituicaoProvedorRepository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final OpenFinanceCredencialService credencialService;
    private final OpenFinanceSincronizacaoService sincronizacaoService;
    private final OpenFinanceProviderFactory providerFactory;

    @Value("${app.openfinance.sync.rate-limit-minutes:15}")
    private int rateLimitMinutes = 15;

    @Value("${app.openfinance.consent.warning-days:30}")
    private int consentWarningDays = 30;

    public OpenFinanceConexaoService(
            OpenFinanceConexaoRepository conexaoRepository,
            OpenFinanceConsentimentoRepository consentimentoRepository,
            OpenFinanceContaExternaRepository contaExternaRepository,
            OpenFinanceInstituicaoProvedorRepository instituicaoProvedorRepository,
            ContaRepository contaRepository,
            CartaoCreditoRepository cartaoCreditoRepository,
            OpenFinanceCredencialService credencialService,
            OpenFinanceSincronizacaoService sincronizacaoService,
            OpenFinanceProviderFactory providerFactory
    ) {
        this.conexaoRepository = conexaoRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.contaExternaRepository = contaExternaRepository;
        this.instituicaoProvedorRepository = instituicaoProvedorRepository;
        this.contaRepository = contaRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.credencialService = credencialService;
        this.sincronizacaoService = sincronizacaoService;
        this.providerFactory = providerFactory;
    }

    @Transactional(readOnly = true)
    public List<OpenFinanceConexaoGridDTO> listarConexoes(Long usuarioId) {
        List<OpenFinanceConexao> conexoes = conexaoRepository.findByUsuarioIdAndDataExclusaoIsNull(usuarioId);
        LocalDateTime agora = LocalDateTime.now();

        return conexoes.stream().map(c -> {
            boolean podeSinc = true;
            long minutosRestantes = 0L;
            if (c.getUltimaSincronizacaoEm() != null) {
                long decorridos = Duration.between(c.getUltimaSincronizacaoEm(), agora).toMinutes();
                if (decorridos < rateLimitMinutes) {
                    podeSinc = false;
                    minutosRestantes = rateLimitMinutes - decorridos;
                }
            }

            OpenFinanceConsentimento cs = c.getConsentimento();
            boolean expirando = false;
            Long diasParaExpirar = null;
            if (cs != null && cs.getDataExpiracao() != null) {
                diasParaExpirar = Duration.between(agora, cs.getDataExpiracao()).toDays();
                expirando = diasParaExpirar <= consentWarningDays && diasParaExpirar >= 0;
            }

            List<OpenFinanceContaExterna> contas = c.getContasExternas() != null ? c.getContasExternas() : new ArrayList<>();
            int totalContas = contas.size();
            int totalVinculadas = (int) contas.stream().filter(ce -> ce.getConta() != null || ce.getCartaoCredito() != null).count();

            return OpenFinanceConexaoGridDTO.builder()
                    .id(c.getId())
                    .idExterno(c.getIdExterno())
                    .status(c.getStatus())
                    .statusDescricao(c.getStatus() != null ? c.getStatus().getDescricao() : null)
                    .statusBadgeClass(obterBadgeClass(c.getStatus()))
                    .statusIcone(obterIcone(c.getStatus()))
                    .statusDetalhe(c.getStatusDetalhe())
                    .ultimaSincronizacaoEm(c.getUltimaSincronizacaoEm())
                    .proximaSincronizacaoEm(c.getProximaSincronizacaoEm())
                    .instituicaoId(c.getInstituicao() != null ? c.getInstituicao().getId() : null)
                    .instituicaoNome(c.getInstituicao() != null ? c.getInstituicao().getNome() : "Não Identificada")
                    .instituicaoCodigo(c.getInstituicao() != null ? c.getInstituicao().getCodigo() : null)
                    .instituicaoCorHex("#206bc4")
                    .provedorId(c.getProvedor().getId())
                    .provedorNome(c.getProvedor().getNome())
                    .provedorCodigo(c.getProvedor().getCodigo())
                    .consentimentoStatus(cs != null ? cs.getStatus() : null)
                    .consentimentoExpiracao(cs != null ? cs.getDataExpiracao() : null)
                    .consentimentoExpirando(expirando)
                    .diasParaExpirar(diasParaExpirar)
                    .totalContas(totalContas)
                    .totalContasVinculadas(totalVinculadas)
                    .podeSincronizar(podeSinc)
                    .minutosParaLiberarSincronizacao(minutosRestantes)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String gerarConnectToken(Long usuarioId, Long conexaoIdOpcional) {
        String itemIdOpcional = null;
        String provedorCodigo = "PLUGGY";

        if (conexaoIdOpcional != null) {
            OpenFinanceConexao conexao = conexaoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(conexaoIdOpcional, usuarioId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.conexao.nao-encontrada"));
            itemIdOpcional = conexao.getIdExterno();
            provedorCodigo = conexao.getProvedor().getCodigo();
        }

        OpenFinanceCredencial credencial = credencialService.obterCredencialAtivaPorProvedorCodigo(usuarioId, provedorCodigo);
        String secret = credencialService.obterClientSecretDecifrado(credencial);
        OpenFinanceProviderStrategy strategy = providerFactory.obterStrategy(provedorCodigo);

        return strategy.gerarConnectToken(credencial.getClientId(), secret, credencial.getAmbiente(), itemIdOpcional);
    }

    @Transactional
    public OpenFinanceConexao processarCallback(Long usuarioId, String itemId) {
        String provedorCodigo = "PLUGGY";
        OpenFinanceCredencial credencial = credencialService.obterCredencialAtivaPorProvedorCodigo(usuarioId, provedorCodigo);
        String secret = credencialService.obterClientSecretDecifrado(credencial);
        OpenFinanceProviderStrategy strategy = providerFactory.obterStrategy(provedorCodigo);

        PluggyItemDTO item = strategy.buscarItem(credencial.getClientId(), secret, credencial.getAmbiente(), itemId);
        if (item == null) {
            throw new RegraNegocioException("openfinance.callback.item-invalido");
        }

        Optional<OpenFinanceConexao> optExistente = conexaoRepository
                .findByIdExternoAndProvedorIdAndDataExclusaoIsNull(itemId, credencial.getProvedor().getId());

        OpenFinanceConexao conexao = optExistente.orElseGet(OpenFinanceConexao::new);
        conexao.setUsuario(credencial.getUsuario());
        conexao.setProvedor(credencial.getProvedor());
        conexao.setCredencial(credencial);
        conexao.setIdExterno(itemId);
        conexao.setStatus(mapearStatusItem(item.getStatus()));
        conexao.setExecucaoStatus(item.getExecutionStatus());

        if (item.getConnector() != null && item.getConnector().getId() != null) {
            String connectorIdStr = String.valueOf(item.getConnector().getId());
            Optional<OpenFinanceInstituicaoProvedor> mapInst = instituicaoProvedorRepository
                    .findByProvedorIdAndIdExternoAndDataExclusaoIsNull(credencial.getProvedor().getId(), connectorIdStr);
            if (mapInst.isPresent()) {
                conexao.setInstituicao(mapInst.get().getInstituicao());
            }
        }

        conexao = conexaoRepository.save(conexao);

        // Espelho de Consentimento
        Optional<OpenFinanceConsentimento> optConsentimento = consentimentoRepository.findByConexaoIdAndDataExclusaoIsNull(conexao.getId());
        OpenFinanceConsentimento consentimento = optConsentimento.orElseGet(OpenFinanceConsentimento::new);
        consentimento.setConexao(conexao);
        consentimento.setIdExterno(itemId);
        consentimento.setEscopos("ACCOUNTS,TRANSACTIONS,CREDIT_CARDS");
        consentimento.setDataConcessao(LocalDateTime.now());
        consentimento.setDataExpiracao(LocalDateTime.now().plusMonths(12));
        consentimento.setStatus(StatusConsentimento.ATIVO);
        consentimentoRepository.save(consentimento);
        conexao.setConsentimento(consentimento);

        // Dispara sincronização inicial em background
        sincronizacaoService.sincronizarConexaoAssincrona(conexao.getId(), TipoSincronizacao.MANUAL);

        return conexao;
    }

    @Transactional
    public void desconectar(Long usuarioId, Long conexaoId) {
        OpenFinanceConexao conexao = conexaoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(conexaoId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.conexao.nao-encontrada"));

        try {
            OpenFinanceCredencial credencial = conexao.getCredencial();
            String secret = credencialService.obterClientSecretDecifrado(credencial);
            OpenFinanceProviderStrategy strategy = providerFactory.obterStrategy(conexao.getProvedor().getCodigo());
            strategy.revogarItem(credencial.getClientId(), secret, credencial.getAmbiente(), conexao.getIdExterno());
        } catch (Exception e) {
            log.warn("Falha ao revogar item {} no provedor externo: {}", conexao.getIdExterno(), e.getMessage());
        }

        Instant agora = Instant.now();
        conexao.setDataExclusao(agora);

        OpenFinanceConsentimento consentimento = conexao.getConsentimento();
        if (consentimento != null) {
            consentimento.setStatus(StatusConsentimento.REVOGADO);
            consentimento.setDataRevogacao(LocalDateTime.now());
            consentimento.setDataExclusao(agora);
            consentimentoRepository.save(consentimento);
        }

        List<OpenFinanceContaExterna> contas = contaExternaRepository.findByConexaoIdAndDataExclusaoIsNull(conexao.getId());
        for (OpenFinanceContaExterna ce : contas) {
            ce.setDataExclusao(agora);
            contaExternaRepository.save(ce);
        }

        conexaoRepository.save(conexao);
    }

    @Transactional
    public OpenFinanceContaExterna vincularContaExterna(Long usuarioId, Long contaExternaId, Long ctaId, Long cacrId) {
        OpenFinanceContaExterna contaExterna = contaExternaRepository.findByIdAndConexaoUsuarioIdAndDataExclusaoIsNull(contaExternaId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.conta-externa.nao-encontrada"));

        if (contaExterna.getTipo() == TipoContaExterna.BANK) {
            if (cacrId != null) {
                throw new RegraNegocioException("openfinance.vinculo.tipo-incompativel");
            }
            if (ctaId != null) {
                Conta conta = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(ctaId, usuarioId)
                        .orElseThrow(() -> new RegistroNaoEncontradoException("conta.nao-encontrada"));
                contaExterna.setConta(conta);
            } else {
                contaExterna.setConta(null);
            }
            contaExterna.setCartaoCredito(null);
        } else if (contaExterna.getTipo() == TipoContaExterna.CREDIT) {
            if (ctaId != null) {
                throw new RegraNegocioException("openfinance.vinculo.tipo-incompativel");
            }
            if (cacrId != null) {
                CartaoCredito cartao = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(cacrId, usuarioId)
                        .orElseThrow(() -> new RegistroNaoEncontradoException("cartao.nao-encontrado"));
                contaExterna.setCartaoCredito(cartao);
            } else {
                contaExterna.setCartaoCredito(null);
            }
            contaExterna.setConta(null);
        }

        return contaExternaRepository.save(contaExterna);
    }

    @Transactional(readOnly = true)
    public List<OpenFinanceContaExternaDTO> buscarContasExternasPorConexao(Long usuarioId, Long conexaoId) {
        OpenFinanceConexao conexao = conexaoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(conexaoId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.conexao.nao-encontrada"));

        return contaExternaRepository.findByConexaoIdAndDataExclusaoIsNull(conexao.getId()).stream()
                .map(ce -> OpenFinanceContaExternaDTO.builder()
                        .id(ce.getId())
                        .idExterno(ce.getIdExterno())
                        .tipo(ce.getTipo())
                        .tipoDescricao(ce.getTipo() != null ? ce.getTipo().getDescricao() : null)
                        .subtipo(ce.getSubtipo())
                        .nome(ce.getNome())
                        .numero(ce.getNumero())
                        .saldo(ce.getSaldo())
                        .saldoEm(ce.getSaldoEm())
                        .moeda(ce.getMoeda())
                        .contaId(ce.getConta() != null ? ce.getConta().getId() : null)
                        .contaDescricao(ce.getConta() != null ? ce.getConta().getDescricao() : null)
                        .cartaoId(ce.getCartaoCredito() != null ? ce.getCartaoCredito().getId() : null)
                        .cartaoDescricao(ce.getCartaoCredito() != null ? ce.getCartaoCredito().getDescricao() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private StatusConexao mapearStatusItem(String pluggyStatus) {
        if (pluggyStatus == null) {
            return StatusConexao.ATUALIZANDO;
        }
        return switch (pluggyStatus.toUpperCase()) {
            case "UPDATED" -> StatusConexao.ATUALIZADO;
            case "UPDATING" -> StatusConexao.ATUALIZANDO;
            case "LOGIN_ERROR" -> StatusConexao.ERRO_LOGIN;
            case "WAITING_USER_INPUT" -> StatusConexao.AGUARDANDO_USUARIO;
            case "OUTDATED" -> StatusConexao.DESATUALIZADO;
            default -> StatusConexao.ATUALIZANDO;
        };
    }

    private String obterBadgeClass(StatusConexao status) {
        if (status == null) return "bg-secondary-lt";
        return switch (status) {
            case ATUALIZADO -> "bg-success-lt";
            case ATUALIZANDO -> "bg-azure-lt";
            case ERRO_LOGIN -> "bg-danger-lt";
            case DESATUALIZADO -> "bg-warning-lt";
            case AGUARDANDO_USUARIO -> "bg-purple-lt";
        };
    }

    private String obterIcone(StatusConexao status) {
        if (status == null) return "ph-question";
        return switch (status) {
            case ATUALIZADO -> "ph-check-circle";
            case ATUALIZANDO -> "ph-arrows-clockwise";
            case ERRO_LOGIN -> "ph-warning-circle";
            case DESATUALIZADO -> "ph-clock-countdown";
            case AGUARDANDO_USUARIO -> "ph-user";
        };
    }
}
