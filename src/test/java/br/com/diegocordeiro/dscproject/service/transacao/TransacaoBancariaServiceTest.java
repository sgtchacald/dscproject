package br.com.diegocordeiro.dscproject.service.transacao;

import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFiltroDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFormDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaTotaisDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoBancariaServiceTest {

    @Mock
    private TransacaoBancariaRepository transacaoBancariaRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private TransacaoBancariaService transacaoBancariaService;

    @BeforeEach
    void setUp() {
        transacaoBancariaService = new TransacaoBancariaService(
            transacaoBancariaRepository,
            contaRepository,
            categoriaRepository
        );
    }

    private Conta contaUsuario(Long id, Long usuarioId) {
        Conta c = new Conta();
        c.setId(id);
        c.setDescricao("Conta Corrente Nubank");
        Usuario u = new Usuario();
        u.setId(usuarioId);
        c.setUsuario(u);
        return c;
    }

    private Categoria categoriaValida(Long id) {
        Categoria cat = new Categoria();
        cat.setId(id);
        cat.setNome("Alimentação");
        cat.setAtivo(true);
        return cat;
    }

    private TransacaoBancaria transacaoValida(Long id, Long usuarioId, NaturezaMovimento nat, boolean fatura, OrigemLancamento origem) {
        TransacaoBancaria t = new TransacaoBancaria();
        t.setId(id);
        t.setDescricao("Compra Supermercado");
        t.setValor(new BigDecimal("150.00"));
        t.setCompetencia(YearMonth.of(2026, 9));
        t.setDataLancamento(LocalDate.of(2026, 9, 10));
        t.setConta(contaUsuario(1L, usuarioId));
        t.setNatureza(nat);
        t.setPagamentoFatura(fatura);
        t.setOrigem(origem);
        return t;
    }

    @Test
    @DisplayName("Deve inserir transacao bancaria com sucesso")
    void deveInserirComSucesso() {
        Long usuarioId = 1L;
        TransacaoBancariaFormDTO dto = new TransacaoBancariaFormDTO();
        dto.setCompetencia("2026-09");
        dto.setDataLancamento(LocalDate.of(2026, 9, 10));
        dto.setContaId(1L);
        dto.setNatureza(NaturezaMovimento.DEBITO);
        dto.setDescricao("Supermercado");
        dto.setValor(new BigDecimal("150.00"));
        dto.setCategoriaId(10L);

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(1L, usuarioId))
            .thenReturn(Optional.of(contaUsuario(1L, usuarioId)));
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(10L))
            .thenReturn(Optional.of(categoriaValida(10L)));
        when(transacaoBancariaRepository.save(any(TransacaoBancaria.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        TransacaoBancaria salva = transacaoBancariaService.inserir(dto, usuarioId, "admin");

        assertNotNull(salva);
        assertEquals("Supermercado", salva.getDescricao());
        assertEquals(NaturezaMovimento.DEBITO, salva.getNatureza());
        assertEquals(OrigemLancamento.MANUAL, salva.getOrigem());
        assertFalse(salva.isPagamentoFatura());
        verify(transacaoBancariaRepository).save(any(TransacaoBancaria.class));
    }

    @Test
    @DisplayName("Deve buscar transacao para edicao com sucesso")
    void deveBuscarParaEdicaoComSucesso() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.CREDITO, false, OrigemLancamento.MANUAL);
        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));

        TransacaoBancariaEdicaoDTO dto = transacaoBancariaService.buscarParaEdicao(100L, usuarioId);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("Compra Supermercado", dto.getDescricao());
        assertEquals(NaturezaMovimento.CREDITO, dto.getNatureza());
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar transacao inexistente ou de outro usuario")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(999L, 1L))
            .thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () ->
            transacaoBancariaService.buscarParaEdicao(999L, 1L));
    }

    @Test
    @DisplayName("Deve bloquear alteracao de conta em transacao de fatura (RN08)")
    void deveBloquearAlteracaoContaEmFatura() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.DEBITO, true, OrigemLancamento.MANUAL);

        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));

        TransacaoBancariaFormDTO dto = new TransacaoBancariaFormDTO();
        dto.setContaId(2L); // Mudou a conta
        dto.setValor(new BigDecimal("150.00"));
        dto.setCompetencia("2026-09");
        dto.setDataLancamento(LocalDate.of(2026, 9, 10));
        dto.setNatureza(NaturezaMovimento.DEBITO);
        dto.setDescricao("Pagamento Fatura");

        assertThrows(RegraNegocioException.class, () ->
            transacaoBancariaService.editar(100L, dto, usuarioId, "admin"));
    }

    @Test
    @DisplayName("Deve bloquear alteracao de conta em transacao Open Finance (RN09)")
    void deveBloquearAlteracaoContaEmOpenFinance() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.CREDITO, false, OrigemLancamento.OPEN_FINANCE);

        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));

        TransacaoBancariaFormDTO dto = new TransacaoBancariaFormDTO();
        dto.setContaId(2L); // Mudou a conta
        dto.setValor(new BigDecimal("150.00"));
        dto.setCompetencia("2026-09");
        dto.setDataLancamento(LocalDate.of(2026, 9, 10));
        dto.setNatureza(NaturezaMovimento.CREDITO);
        dto.setDescricao("Transação OF");

        assertThrows(RegraNegocioException.class, () ->
            transacaoBancariaService.editar(100L, dto, usuarioId, "admin"));
    }

    @Test
    @DisplayName("Deve bloquear exclusao de transacao vinculada a fatura (RN08)")
    void deveBloquearExclusaoTransacaoFatura() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.DEBITO, true, OrigemLancamento.MANUAL);
        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));

        assertThrows(RegraNegocioException.class, () ->
            transacaoBancariaService.excluir(100L, usuarioId, "admin"));
    }

    @Test
    @DisplayName("Deve bloquear exclusao de transacao vinculada a Open Finance (RN09)")
    void deveBloquearExclusaoTransacaoOpenFinance() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.CREDITO, false, OrigemLancamento.OPEN_FINANCE);
        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));

        assertThrows(RegraNegocioException.class, () ->
            transacaoBancariaService.excluir(100L, usuarioId, "admin"));
    }

    @Test
    @DisplayName("Deve duplicar transacao para outra competencia com sucesso")
    void deveDuplicarComSucesso() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.DEBITO, false, OrigemLancamento.MANUAL);
        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));
        when(transacaoBancariaRepository.save(any(TransacaoBancaria.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        TransacaoBancaria dup = transacaoBancariaService.duplicar(100L, "2026-10", null, usuarioId, "admin");

        assertNotNull(dup);
        assertEquals(YearMonth.of(2026, 10), dup.getCompetencia());
        assertEquals(LocalDate.of(2026, 10, 10), dup.getDataLancamento());
        assertEquals(OrigemLancamento.MANUAL, dup.getOrigem());
        assertFalse(dup.isPagamentoFatura());
    }

    @Test
    @DisplayName("Deve bloquear duplicacao de transacao para a mesma competencia")
    void deveBloquearDuplicacaoMesmaCompetencia() {
        Long usuarioId = 1L;
        TransacaoBancaria t = transacaoValida(100L, usuarioId, NaturezaMovimento.DEBITO, false, OrigemLancamento.MANUAL);
        when(transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(100L, usuarioId))
            .thenReturn(Optional.of(t));

        assertThrows(RegraNegocioException.class, () ->
            transacaoBancariaService.duplicar(100L, "2026-09", null, usuarioId, "admin"));
    }

    @Test
    @DisplayName("Deve calcular totalizadores de competencia unica")
    void deveCalcularTotalizadoresCompetenciaUnica() {
        Long usuarioId = 1L;
        TransacaoBancariaFiltroDTO filtro = new TransacaoBancariaFiltroDTO();
        filtro.setCompetenciaInicio("2026-09");
        filtro.setCompetenciaFim("2026-09");

        when(transacaoBancariaRepository.somarPorCompetenciaENaturezaEUsuario(eq(YearMonth.of(2026, 9)), eq(NaturezaMovimento.CREDITO), eq(usuarioId)))
            .thenReturn(new BigDecimal("1000.00"));
        when(transacaoBancariaRepository.somarPorCompetenciaENaturezaEUsuario(eq(YearMonth.of(2026, 9)), eq(NaturezaMovimento.DEBITO), eq(usuarioId)))
            .thenReturn(new BigDecimal("400.00"));

        TransacaoBancariaTotaisDTO totais = transacaoBancariaService.calcularTotais(filtro, usuarioId);

        assertNotNull(totais);
        assertEquals(new BigDecimal("1000.00"), totais.getTotalCreditos());
        assertEquals(new BigDecimal("400.00"), totais.getTotalDebitos());
        assertEquals(new BigDecimal("600.00"), totais.getSaldoLiquido());
    }
}
