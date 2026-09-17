package br.com.diegocordeiro.dscproject.service.fatura;

import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCompraItemDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaEncargosDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaFechamentoDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCartaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaPagamentoDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusFatura;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.fatura.FaturaCartao;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.fatura.FaturaCartaoRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class FaturaCartaoService {

    private final FaturaCartaoRepository faturaCartaoRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final DespesaRepository despesaRepository;
    private final ContaRepository contaRepository;
    private final TransacaoBancariaRepository transacaoBancariaRepository;

    public FaturaCartaoService(FaturaCartaoRepository faturaCartaoRepository, CartaoCreditoRepository cartaoCreditoRepository, DespesaRepository despesaRepository, ContaRepository contaRepository, TransacaoBancariaRepository transacaoBancariaRepository) {
        this.faturaCartaoRepository = faturaCartaoRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.despesaRepository = despesaRepository;
        this.contaRepository = contaRepository;
        this.transacaoBancariaRepository = transacaoBancariaRepository;
    }

    @Transactional
    public List<FaturaCartaoGridDTO> listarFaturas(Long cartaoId, Long usuarioId) {
        CartaoCredito cartao = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(cartaoId, usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.cartao.nao-encontrado"));

        sincronizarFaturasAbertas(cartao);

        List<FaturaCartao> faturas = faturaCartaoRepository.listarPorCartaoEUsuario(cartaoId, usuarioId);

        return faturas.stream().map(f -> {
            BigDecimal totalCompras;
            BigDecimal valorTotal;

            if (f.getStatus() == StatusFatura.ABERTA) {
                totalCompras = despesaRepository.somarPorCartaoECompetencia(cartaoId, f.getCompetencia());
                if (totalCompras == null) totalCompras = BigDecimal.ZERO;
                BigDecimal encargos = f.getValorEncargos() != null ? f.getValorEncargos() : BigDecimal.ZERO;
                valorTotal = totalCompras.add(encargos);
            } else {
                valorTotal = f.getValorTotal() != null ? f.getValorTotal() : BigDecimal.ZERO;
                BigDecimal encargos = f.getValorEncargos() != null ? f.getValorEncargos() : BigDecimal.ZERO;
                totalCompras = valorTotal.subtract(encargos);
            }

            return FaturaCartaoGridDTO.builder()
                .id(f.getId())
                .cartaoId(cartao.getId())
                .cartaoDescricao(cartao.getDescricao())
                .competencia(f.getCompetencia())
                .dataFechamento(f.getDataFechamento())
                .dataVencimento(f.getDataVencimento())
                .totalCompras(totalCompras)
                .valorEncargos(f.getValorEncargos())
                .valorTotal(valorTotal)
                .valorMinimo(f.getValorMinimo())
                .valorPago(f.getValorPago())
                .status(f.getStatus())
                .origem(f.getOrigem())
                .transacaoPagamentoId(f.getTransacaoPagamento() != null ? f.getTransacaoPagamento().getId() : null)
                .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<FaturaCompraItemDTO> listarComprasDaFatura(Long faturaId, Long usuarioId) {
        FaturaCartao fatura = buscarPorIdEUsuario(faturaId, usuarioId);
        List<Despesa> despesas = despesaRepository.listarPorCartaoECompetencia(fatura.getCartao().getId(), fatura.getCompetencia());

        return despesas.stream().map(d -> {
            String parcelaStr = (d.getNroParcela() != null && d.getQtdParcelas() != null)
                ? d.getNroParcela() + "/" + d.getQtdParcelas()
                : "-";

            return FaturaCompraItemDTO.builder()
                .despesaId(d.getId())
                .data(d.getDataLancamento())
                .descricao(d.getNome())
                .categoriaNome(d.getCategoria() != null ? d.getCategoria().getNome() : "Sem categoria")
                .parcela(parcelaStr)
                .valor(d.getValor())
                .cotaTitular(d.getValor())
                .build();
        }).toList();
    }

    @Transactional
    public FaturaCartao fecharFatura(Long faturaId, FaturaFechamentoDTO dto, Long usuarioId, String usuarioAuditoria) {
        FaturaCartao fatura = buscarPorIdEUsuario(faturaId, usuarioId);

        if (fatura.getStatus() != StatusFatura.ABERTA) {
            throw new RegraNegocioException("msg.fatura.status.invalido-fechamento");
        }

        if (dto.getDataFechamento().isAfter(dto.getDataVencimento())) {
            throw new RegraNegocioException("dataFechamento", "msg.fatura.fechamento.data-invalida");
        }

        BigDecimal encargos = dto.getValorEncargos() != null ? dto.getValorEncargos() : BigDecimal.ZERO;
        if (encargos.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("valorEncargos", "msg.fatura.fechamento.encargos-negativos");
        }

        BigDecimal compras = despesaRepository.somarPorCartaoECompetencia(fatura.getCartao().getId(), fatura.getCompetencia());
        if (compras == null) compras = BigDecimal.ZERO;

        BigDecimal total = compras.add(encargos);

        fatura.setDataFechamento(dto.getDataFechamento());
        fatura.setDataVencimento(dto.getDataVencimento());
        fatura.setValorEncargos(encargos);
        fatura.setValorTotal(total);
        fatura.setValorMinimo(dto.getValorMinimo());
        fatura.setStatus(StatusFatura.FECHADA);
        fatura.setAlteradoPor(usuarioAuditoria);

        return faturaCartaoRepository.save(fatura);
    }

    @Transactional
    public FaturaCartao reabrirFatura(Long faturaId, Long usuarioId, String usuarioAuditoria) {
        FaturaCartao fatura = buscarPorIdEUsuario(faturaId, usuarioId);

        if (fatura.getStatus() != StatusFatura.FECHADA) {
            throw new RegraNegocioException("msg.fatura.status.invalido-reabertura");
        }

        if (fatura.getValorPago().compareTo(BigDecimal.ZERO) > 0 || fatura.getTransacaoPagamento() != null) {
            throw new RegraNegocioException("msg.fatura.status.ja-paga");
        }

        if (fatura.getOrigem() == OrigemLancamento.OPEN_FINANCE) {
            throw new RegraNegocioException("msg.fatura.open-finance.imutavel");
        }

        fatura.setStatus(StatusFatura.ABERTA);
        fatura.setValorTotal(null);
        fatura.setDataFechamento(null);
        fatura.setAlteradoPor(usuarioAuditoria);

        return faturaCartaoRepository.save(fatura);
    }

    @Transactional
    public FaturaCartao registrarPagamento(Long faturaId, FaturaPagamentoDTO dto, Long usuarioId, String usuarioAuditoria) {
        FaturaCartao fatura = buscarPorIdEUsuario(faturaId, usuarioId);

        if (fatura.getStatus() == StatusFatura.ABERTA) {
            throw new RegraNegocioException("msg.fatura.status.aberta-pagamento-bloqueado");
        }

        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new RegraNegocioException("msg.fatura.status.ja-paga");
        }

        Conta conta = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId)
            .filter(Conta::isAtivo)
            .orElseThrow(() -> new RegraNegocioException("contaId", "msg.fatura.pagamento.conta-invalida"));

        if (dto.getValorPago() == null || dto.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("valorPago", "msg.fatura.pagamento.valor-invalido");
        }

        BigDecimal novoValorPago = fatura.getValorPago().add(dto.getValorPago());
        StatusFatura novoStatus = (novoValorPago.compareTo(fatura.getValorTotal()) >= 0)
            ? StatusFatura.PAGA
            : StatusFatura.PAGA_PARCIAL;

        TransacaoBancaria debito = new TransacaoBancaria();
        debito.setDescricao("Pagamento Fatura " + fatura.getCartao().getDescricao() + " (" + fatura.getCompetencia() + ")");
        debito.setValor(dto.getValorPago());
        debito.setNatureza(NaturezaMovimento.DEBITO);
        debito.setDataLancamento(dto.getDataPagamento());
        debito.setCompetencia(YearMonth.from(dto.getDataPagamento()));
        debito.setConta(conta);
        debito.setOrigem(OrigemLancamento.MANUAL);
        debito.setPagamentoFatura(true);

        transacaoBancariaRepository.save(debito);

        fatura.setValorPago(novoValorPago);
        fatura.setStatus(novoStatus);
        fatura.setTransacaoPagamento(debito);
        fatura.setAlteradoPor(usuarioAuditoria);

        return faturaCartaoRepository.save(fatura);
    }

    @Transactional
    public FaturaCartao editarEncargos(Long faturaId, FaturaEncargosDTO dto, Long usuarioId, String usuarioAuditoria) {
        FaturaCartao fatura = buscarPorIdEUsuario(faturaId, usuarioId);

        if (fatura.getStatus() != StatusFatura.FECHADA) {
            throw new RegraNegocioException("msg.fatura.status.invalido-edicao-encargos");
        }

        if (fatura.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraNegocioException("msg.fatura.status.ja-paga");
        }

        BigDecimal novosEncargos = dto.getValorEncargos() != null ? dto.getValorEncargos() : BigDecimal.ZERO;
        if (novosEncargos.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("valorEncargos", "msg.fatura.fechamento.encargos-negativos");
        }

        BigDecimal compras = despesaRepository.somarPorCartaoECompetencia(fatura.getCartao().getId(), fatura.getCompetencia());
        if (compras == null) compras = BigDecimal.ZERO;

        fatura.setValorEncargos(novosEncargos);
        fatura.setValorTotal(compras.add(novosEncargos));
        if (dto.getValorMinimo() != null) {
            fatura.setValorMinimo(dto.getValorMinimo());
        }
        fatura.setAlteradoPor(usuarioAuditoria);

        return faturaCartaoRepository.save(fatura);
    }

    @Transactional(readOnly = true)
    public FaturaCartao buscarPorIdEUsuario(Long id, Long usuarioId) {
        return faturaCartaoRepository.findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.fatura.nao-encontrada"));
    }

    private void sincronizarFaturasAbertas(CartaoCredito cartao) {
        Set<YearMonth> competencias = new LinkedHashSet<>();
        competencias.add(YearMonth.now());
        competencias.add(YearMonth.now().minusMonths(1));

        List<YearMonth> compsDespesas = despesaRepository.listarCompetenciasPorCartao(cartao.getId());
        if (compsDespesas != null) {
            competencias.addAll(compsDespesas);
        }

        for (YearMonth comp : competencias) {
            if (!faturaCartaoRepository.existsByCartaoIdAndCompetenciaAndDataExclusaoIsNull(cartao.getId(), comp)) {
                LocalDate vencimento = calcularDataVencimento(cartao, comp);
                FaturaCartao nova = new FaturaCartao();
                nova.setCartao(cartao);
                nova.setCompetencia(comp);
                nova.setDataVencimento(vencimento);
                nova.setValorEncargos(BigDecimal.ZERO);
                nova.setValorPago(BigDecimal.ZERO);
                nova.setStatus(StatusFatura.ABERTA);
                nova.setOrigem(OrigemLancamento.MANUAL);
                faturaCartaoRepository.save(nova);
            }
        }
    }

    private LocalDate calcularDataVencimento(CartaoCredito cartao, YearMonth comp) {
        int dia = (cartao.getDiaVencimento() != null && cartao.getDiaVencimento() > 0)
            ? cartao.getDiaVencimento()
            : 10;
        int maxDia = comp.lengthOfMonth();
        return comp.atDay(Math.min(dia, maxDia));
    }
}
