package br.com.diegocordeiro.dscproject.service.fatura;

import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCartaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCompraItemDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaEncargosDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaFechamentoDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaPagamentoDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusFatura;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.fatura.FaturaCartao;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.fatura.FaturaCartaoRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaturaCartaoServiceTest {

    @Mock
    private FaturaCartaoRepository faturaCartaoRepository;

    @Mock
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Mock
    private DespesaRepository despesaRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoBancariaRepository transacaoBancariaRepository;

    private FaturaCartaoService faturaCartaoService;

    @BeforeEach
    void setUp() {
        faturaCartaoService = new FaturaCartaoService(
            faturaCartaoRepository,
            cartaoCreditoRepository,
            despesaRepository,
            contaRepository,
            transacaoBancariaRepository
        );
    }

    private CartaoCredito cartaoUsuario(Long id, Long usuarioId) {
        CartaoCredito c = new CartaoCredito();
        c.setId(id);
        c.setDescricao("Nubank Ultravioleta");
        c.setDiaFechamento(3);
        c.setDiaVencimento(10);
        c.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        c.setUsuario(u);
        return c;
    }

    private Conta contaUsuario(Long id, Long usuarioId) {
        Conta c = new Conta();
        c.setId(id);
        c.setDescricao("Banco do Brasil");
        c.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        c.setUsuario(u);
        return c;
    }

    private FaturaCartao faturaAberta(Long id, CartaoCredito cartao, YearMonth comp) {
        FaturaCartao f = new FaturaCartao();
        f.setId(id);
        f.setCartao(cartao);
        f.setCompetencia(comp);
        f.setDataVencimento(comp.atDay(10));
        f.setStatus(StatusFatura.ABERTA);
        f.setValorEncargos(BigDecimal.ZERO);
        f.setValorPago(BigDecimal.ZERO);
        f.setOrigem(OrigemLancamento.MANUAL);
        return f;
    }

    @Test
    @DisplayName("BDD 16.0 - Deve calcular total dinamico de compras em fatura aberta")
    void deveCalcularTotalDinamicoFaturaAberta() {
        Long usuarioId = 1L;
        Long cartaoId = 10L;
        CartaoCredito cartao = cartaoUsuario(cartaoId, usuarioId);
        YearMonth comp = YearMonth.of(2026, 10);
        FaturaCartao f = faturaAberta(100L, cartao, comp);

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(cartaoId, usuarioId))
            .thenReturn(Optional.of(cartao));
        when(faturaCartaoRepository.listarPorCartaoEUsuario(cartaoId, usuarioId))
            .thenReturn(List.of(f));
        when(despesaRepository.somarPorCartaoECompetencia(cartaoId, comp))
            .thenReturn(new BigDecimal("350.00"));

        List<FaturaCartaoGridDTO> grid = faturaCartaoService.listarFaturas(cartaoId, usuarioId);

        assertNotNull(grid);
        assertEquals(1, grid.size());
        FaturaCartaoGridDTO item = grid.get(0);
        assertEquals(new BigDecimal("350.00"), item.getTotalCompras());
        assertEquals(new BigDecimal("350.00"), item.getValorTotal());
        assertEquals(StatusFatura.ABERTA, item.getStatus());
    }

    @Test
    @DisplayName("BDD 16.1 - Deve fechar fatura congelando total de compras e encargos")
    void deveFecharFaturaComSucesso() {
        Long usuarioId = 1L;
        CartaoCredito cartao = cartaoUsuario(10L, usuarioId);
        YearMonth comp = YearMonth.of(2026, 10);
        FaturaCartao f = faturaAberta(100L, cartao, comp);

        when(faturaCartaoRepository.findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(f));
        when(despesaRepository.somarPorCartaoECompetencia(10L, comp))
            .thenReturn(new BigDecimal("350.00"));
        when(faturaCartaoRepository.save(any(FaturaCartao.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        FaturaFechamentoDTO dto = FaturaFechamentoDTO.builder()
            .dataFechamento(LocalDate.of(2026, 10, 3))
            .dataVencimento(LocalDate.of(2026, 10, 10))
            .valorEncargos(new BigDecimal("15.00"))
            .build();

        FaturaCartao fechada = faturaCartaoService.fecharFatura(100L, dto, usuarioId, "admin");

        assertNotNull(fechada);
        assertEquals(StatusFatura.FECHADA, fechada.getStatus());
        assertEquals(new BigDecimal("365.00"), fechada.getValorTotal());
        assertEquals(new BigDecimal("15.00"), fechada.getValorEncargos());
        assertEquals(LocalDate.of(2026, 10, 3), fechada.getDataFechamento());
    }

    @Test
    @DisplayName("BDD 16.2 - Deve registrar pagamento criando transacao bancaria de debito")
    void deveRegistrarPagamentoComTransacaoBancaria() {
        Long usuarioId = 1L;
        CartaoCredito cartao = cartaoUsuario(10L, usuarioId);
        YearMonth comp = YearMonth.of(2026, 10);

        FaturaCartao f = faturaAberta(100L, cartao, comp);
        f.setStatus(StatusFatura.FECHADA);
        f.setValorTotal(new BigDecimal("365.00"));
        f.setDataFechamento(LocalDate.of(2026, 10, 3));

        Conta conta = contaUsuario(50L, usuarioId);

        when(faturaCartaoRepository.findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(f));
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, usuarioId))
            .thenReturn(Optional.of(conta));
        when(faturaCartaoRepository.save(any(FaturaCartao.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        FaturaPagamentoDTO dto = FaturaPagamentoDTO.builder()
            .contaId(50L)
            .dataPagamento(LocalDate.of(2026, 10, 10))
            .valorPago(new BigDecimal("365.00"))
            .build();

        FaturaCartao paga = faturaCartaoService.registrarPagamento(100L, dto, usuarioId, "admin");

        assertNotNull(paga);
        assertEquals(StatusFatura.PAGA, paga.getStatus());
        assertEquals(new BigDecimal("365.00"), paga.getValorPago());
        assertNotNull(paga.getTransacaoPagamento());

        ArgumentCaptor<TransacaoBancaria> captor = ArgumentCaptor.forClass(TransacaoBancaria.class);
        verify(transacaoBancariaRepository).save(captor.capture());
        TransacaoBancaria tr = captor.getValue();

        assertEquals(new BigDecimal("365.00"), tr.getValor());
        assertEquals(NaturezaMovimento.DEBITO, tr.getNatureza());
        assertTrue(tr.isPagamentoFatura());
        assertEquals(conta, tr.getConta());
    }

    @Test
    @DisplayName("BDD 16.3 - Deve bloquear reabertura de fatura que ja possui pagamento")
    void deveBloquearReaberturaFaturaPaga() {
        Long usuarioId = 1L;
        CartaoCredito cartao = cartaoUsuario(10L, usuarioId);
        YearMonth comp = YearMonth.of(2026, 10);

        FaturaCartao f = faturaAberta(100L, cartao, comp);
        f.setStatus(StatusFatura.PAGA);
        f.setValorTotal(new BigDecimal("365.00"));
        f.setValorPago(new BigDecimal("365.00"));

        when(faturaCartaoRepository.findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(f));

        assertThrows(RegraNegocioException.class, () ->
            faturaCartaoService.reabrirFatura(100L, usuarioId, "admin"));
    }

    @Test
    @DisplayName("Deve reabrir fatura fechada sem pagamento com sucesso")
    void deveReabrirFaturaFechada() {
        Long usuarioId = 1L;
        CartaoCredito cartao = cartaoUsuario(10L, usuarioId);
        YearMonth comp = YearMonth.of(2026, 10);

        FaturaCartao f = faturaAberta(100L, cartao, comp);
        f.setStatus(StatusFatura.FECHADA);
        f.setValorTotal(new BigDecimal("365.00"));
        f.setDataFechamento(LocalDate.of(2026, 10, 3));

        when(faturaCartaoRepository.findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(f));
        when(faturaCartaoRepository.save(any(FaturaCartao.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        FaturaCartao reaberta = faturaCartaoService.reabrirFatura(100L, usuarioId, "admin");

        assertNotNull(reaberta);
        assertEquals(StatusFatura.ABERTA, reaberta.getStatus());
        assertNull(reaberta.getValorTotal());
        assertNull(reaberta.getDataFechamento());
    }

    @Test
    @DisplayName("Deve listar compras vinculadas a fatura com sucesso")
    void deveListarComprasDaFatura() {
        Long usuarioId = 1L;
        CartaoCredito cartao = cartaoUsuario(10L, usuarioId);
        YearMonth comp = YearMonth.of(2026, 10);
        FaturaCartao f = faturaAberta(100L, cartao, comp);

        Despesa d = new Despesa();
        d.setId(1L);
        d.setNome("Supermercado");
        d.setValor(new BigDecimal("120.00"));
        d.setDataLancamento(LocalDate.of(2026, 10, 2));
        Categoria cat = new Categoria();
        cat.setNome("Alimentação");
        d.setCategoria(cat);

        when(faturaCartaoRepository.findByIdAndCartaoUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(f));
        when(despesaRepository.listarPorCartaoECompetencia(10L, comp))
            .thenReturn(List.of(d));

        List<FaturaCompraItemDTO> itens = faturaCartaoService.listarComprasDaFatura(100L, usuarioId);

        assertNotNull(itens);
        assertEquals(1, itens.size());
        assertEquals("Supermercado", itens.get(0).getDescricao());
        assertEquals(new BigDecimal("120.00"), itens.get(0).getValor());
    }
}
