package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceSincronizacaoDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyAccountDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyBillDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyTransactionDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.StatusConciliacao;
import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.enums.StatusSincronizacao;
import br.com.diegocordeiro.dscproject.enums.TipoContaExterna;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceContaExterna;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceFatura;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceSincronizacao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceTransacao;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConexaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceContaExternaRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceFaturaRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceSincronizacaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceTransacaoRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderFactory;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OpenFinanceSincronizacaoService {

    private static final Logger log = LoggerFactory.getLogger(OpenFinanceSincronizacaoService.class);

    private final OpenFinanceConexaoRepository conexaoRepository;
    private final OpenFinanceContaExternaRepository contaExternaRepository;
    private final OpenFinanceTransacaoRepository transacaoRepository;
    private final OpenFinanceFaturaRepository faturaRepository;
    private final OpenFinanceSincronizacaoRepository sincronizacaoRepository;
    private final OpenFinanceCredencialService credencialService;
    private final OpenFinanceProviderFactory providerFactory;

    @Value("${app.openfinance.sync.rate-limit-minutes:15}")
    private int rateLimitMinutes = 15;

    public OpenFinanceSincronizacaoService(
            OpenFinanceConexaoRepository conexaoRepository,
            OpenFinanceContaExternaRepository contaExternaRepository,
            OpenFinanceTransacaoRepository transacaoRepository,
            OpenFinanceFaturaRepository faturaRepository,
            OpenFinanceSincronizacaoRepository sincronizacaoRepository,
            OpenFinanceCredencialService credencialService,
            OpenFinanceProviderFactory providerFactory
    ) {
        this.conexaoRepository = conexaoRepository;
        this.contaExternaRepository = contaExternaRepository;
        this.transacaoRepository = transacaoRepository;
        this.faturaRepository = faturaRepository;
        this.sincronizacaoRepository = sincronizacaoRepository;
        this.credencialService = credencialService;
        this.providerFactory = providerFactory;
    }

    @Async
    public void sincronizarConexaoAssincrona(Long conexaoId, TipoSincronizacao tipo) {
        try {
            sincronizarConexao(conexaoId, tipo, null);
        } catch (Exception e) {
            log.error("Erro na sincronizacao assincrona da conexao {}: {}", conexaoId, e.getMessage(), e);
        }
    }

    @Transactional
    public void sincronizarConexao(Long conexaoId, TipoSincronizacao tipo, Long usuarioIdOpcional) {
        OpenFinanceConexao conexao = conexaoRepository.findById(conexaoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.conexao.nao-encontrada"));

        if (usuarioIdOpcional != null && !conexao.getUsuario().getId().equals(usuarioIdOpcional)) {
            throw new RegistroNaoEncontradoException("openfinance.conexao.nao-encontrada");
        }

        if (tipo == TipoSincronizacao.MANUAL && conexao.getUltimaSincronizacaoEm() != null) {
            long decorridos = Duration.between(conexao.getUltimaSincronizacaoEm(), LocalDateTime.now()).toMinutes();
            if (decorridos < rateLimitMinutes) {
                throw new RegraNegocioException("openfinance.sync.rate-limit");
            }
        }

        OpenFinanceSincronizacao sinc = new OpenFinanceSincronizacao();
        sinc.setConexao(conexao);
        sinc.setTipo(tipo);
        sinc.setIniciadoEm(LocalDateTime.now());
        sinc.setStatus(StatusSincronizacao.EXECUTANDO);
        sinc = sincronizacaoRepository.save(sinc);

        conexao.setStatus(StatusConexao.ATUALIZANDO);
        conexaoRepository.save(conexao);

        try {
            OpenFinanceCredencial credencial = conexao.getCredencial();
            String secret = credencialService.obterClientSecretDecifrado(credencial);
            OpenFinanceProviderStrategy strategy = providerFactory.obterStrategy(conexao.getProvedor().getCodigo());

            // 1. Sincronizar contas
            List<PluggyAccountDTO> contasPluggy = strategy.buscarContas(
                    credencial.getClientId(),
                    secret,
                    credencial.getAmbiente(),
                    conexao.getIdExterno()
            );

            int contasAtualizadas = 0;
            for (PluggyAccountDTO acc : contasPluggy) {
                OpenFinanceContaExterna contaExterna = contaExternaRepository.findByIdExterno(acc.getId())
                        .orElseGet(() -> {
                            OpenFinanceContaExterna nova = new OpenFinanceContaExterna();
                            nova.setConexao(conexao);
                            nova.setIdExterno(acc.getId());
                            nova.setTipo("CREDIT".equalsIgnoreCase(acc.getType()) ? TipoContaExterna.CREDIT : TipoContaExterna.BANK);
                            nova.setSubtipo(acc.getSubtype());
                            nova.setMoeda(acc.getCurrencyCode() != null ? acc.getCurrencyCode() : "BRL");
                            return nova;
                        });

                contaExterna.setNome(acc.getName());
                contaExterna.setNumero(acc.getNumber());
                contaExterna.setSaldo(acc.getBalance());
                contaExterna.setSaldoEm(LocalDateTime.now());
                contaExternaRepository.save(contaExterna);
                contasAtualizadas++;
            }

            // 2. Sincronizar transações e faturas (últimos 12 meses)
            LocalDate de = LocalDate.now().minusMonths(12);
            LocalDate ate = LocalDate.now();
            int transacoesNovas = 0;

            List<OpenFinanceContaExterna> contas = contaExternaRepository.findByConexaoIdAndDataExclusaoIsNull(conexao.getId());
            for (OpenFinanceContaExterna ce : contas) {
                List<PluggyTransactionDTO> transacoes = strategy.buscarTransacoes(
                        credencial.getClientId(),
                        secret,
                        credencial.getAmbiente(),
                        ce.getIdExterno(),
                        de,
                        ate
                );

                for (PluggyTransactionDTO tx : transacoes) {
                    if (!transacaoRepository.existsByIdExterno(tx.getId())) {
                        OpenFinanceTransacao openFinanceTx = new OpenFinanceTransacao();
                        openFinanceTx.setIdExterno(tx.getId());
                        openFinanceTx.setContaExterna(ce);
                        openFinanceTx.setDescricao(tx.getDescription() != null ? tx.getDescription() : "Transação");
                        openFinanceTx.setValor(tx.getAmount() != null ? tx.getAmount().abs() : BigDecimal.ZERO);
                        openFinanceTx.setDataTransacao(tx.getDate() != null ? tx.getDate() : LocalDate.now());
                        openFinanceTx.setNatureza("CREDIT".equalsIgnoreCase(tx.getType()) ? NaturezaMovimento.CREDITO : NaturezaMovimento.DEBITO);
                        openFinanceTx.setCategoriaExterna(tx.getCategory());
                        openFinanceTx.setStatusExterno(tx.getStatus());
                        openFinanceTx.setStatusConciliacao(StatusConciliacao.PENDENTE);
                        transacaoRepository.save(openFinanceTx);
                        transacoesNovas++;
                    }
                }

                if (ce.getTipo() == TipoContaExterna.CREDIT) {
                    List<PluggyBillDTO> faturas = strategy.buscarFaturas(
                            credencial.getClientId(),
                            secret,
                            credencial.getAmbiente(),
                            ce.getIdExterno()
                    );
                    for (PluggyBillDTO bill : faturas) {
                        if (!faturaRepository.existsByIdExterno(bill.getId())) {
                            OpenFinanceFatura openFinanceFat = new OpenFinanceFatura();
                            openFinanceFat.setIdExterno(bill.getId());
                            openFinanceFat.setContaExterna(ce);
                            openFinanceFat.setDataVencimento(bill.getDueDate() != null ? bill.getDueDate() : LocalDate.now());
                            openFinanceFat.setDataFechamento(bill.getCloseDate());
                            openFinanceFat.setValorTotal(bill.getTotalAmount());
                            openFinanceFat.setValorMinimo(bill.getMinimumPaymentAmount());
                            openFinanceFat.setStatusConciliacao(StatusConciliacao.PENDENTE);
                            faturaRepository.save(openFinanceFat);
                        }
                    }
                }
            }

            sinc.setFinalizadoEm(LocalDateTime.now());
            sinc.setStatus(StatusSincronizacao.CONCLUIDA);
            sinc.setQtdTransacoesNovas(transacoesNovas);
            sinc.setQtdContasAtualizadas(contasAtualizadas);
            sincronizacaoRepository.save(sinc);

            conexao.setStatus(StatusConexao.ATUALIZADO);
            conexao.setUltimaSincronizacaoEm(LocalDateTime.now());
            conexao.setErroCodigo(null);
            conexao.setErroMensagem(null);
            conexaoRepository.save(conexao);

        } catch (Exception e) {
            log.error("Falha ao sincronizar conexao {}: {}", conexao.getId(), e.getMessage(), e);

            sinc.setFinalizadoEm(LocalDateTime.now());
            sinc.setStatus(StatusSincronizacao.ERRO);
            sinc.setErroMensagem(e.getMessage());
            sincronizacaoRepository.save(sinc);

            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("login")) {
                conexao.setStatus(StatusConexao.ERRO_LOGIN);
            } else {
                conexao.setStatus(StatusConexao.DESATUALIZADO);
            }
            conexao.setErroMensagem(e.getMessage());
            conexaoRepository.save(conexao);

            if (tipo == TipoSincronizacao.MANUAL) {
                throw new RegraNegocioException("openfinance.sync.erro", e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<OpenFinanceSincronizacaoDTO> buscarHistorico(Long usuarioId, Long conexaoId, Pageable pageable) {
        OpenFinanceConexao conexao = conexaoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(conexaoId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.conexao.nao-encontrada"));

        return sincronizacaoRepository.findByConexaoIdOrderByIniciadoEmDesc(conexao.getId(), pageable)
                .map(s -> OpenFinanceSincronizacaoDTO.builder()
                        .id(s.getId())
                        .tipo(s.getTipo())
                        .tipoDescricao(s.getTipo() != null ? s.getTipo().getDescricao() : null)
                        .iniciadoEm(s.getIniciadoEm())
                        .finalizadoEm(s.getFinalizadoEm())
                        .status(s.getStatus())
                        .statusDescricao(s.getStatus() != null ? s.getStatus().getDescricao() : null)
                        .qtdTransacoesNovas(s.getQtdTransacoesNovas())
                        .qtdContasAtualizadas(s.getQtdContasAtualizadas())
                        .erroMensagem(s.getErroMensagem())
                        .build());
    }
}
