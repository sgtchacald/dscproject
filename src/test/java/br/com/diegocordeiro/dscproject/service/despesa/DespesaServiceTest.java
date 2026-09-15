package br.com.diegocordeiro.dscproject.service.despesa;

import br.com.diegocordeiro.dscproject.dto.despesa.DespesaFormDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaGridDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaRateioDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.UsuarioRateioDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.despesa.DespesaUsuario;
import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRapidoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRateioDTO;
import br.com.diegocordeiro.dscproject.enums.StatusContato;
import br.com.diegocordeiro.dscproject.enums.TipoContato;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.contato.ContatoRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaUsuarioRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    @Mock private DespesaRepository despesaRepository;
    @Mock private DespesaUsuarioRepository despesaUsuarioRepository;
    @Mock private ContaRepository contaRepository;
    @Mock private CartaoCreditoRepository cartaoCreditoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private DespesaService despesaService;

    @BeforeEach
    void setUp() {
        despesaService = new DespesaService(
                despesaRepository,
                despesaUsuarioRepository,
                contaRepository,
                cartaoCreditoRepository,
                categoriaRepository,
                contatoRepository,
                usuarioRepository);
    }

    private Conta contaAtiva(Long id, Long usuarioId) {
        Conta c = new Conta();
        c.setId(id);
        c.setDescricao("Conta Corrente");
        c.setTipo(TipoConta.CORRENTE);
        c.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        c.setUsuario(u);
        return c;
    }

    private CartaoCredito cartaoAtivo(Long id, Long usuarioId) {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(id);
        cc.setDescricao("Cartão Teste");
        cc.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        cc.setUsuario(u);
        return cc;
    }

    private DespesaFormDTO dtoSimples(Long contaId) {
        DespesaFormDTO dto = new DespesaFormDTO();
        dto.setNome("Energia Elétrica");
        dto.setValor(new BigDecimal("150.00"));
        dto.setDataLancamento(LocalDate.of(2026, 9, 1));
        dto.setDataVencimento(LocalDate.of(2026, 9, 15));
        dto.setCompetencia("2026-09");
        dto.setFormaPagamento("CONTA");
        dto.setContaId(contaId);
        dto.setMeioPagamento(MeioPagamento.DEBITO);
        dto.setStatusPagamento(StatusPagamento.NAO);
        dto.setParcelada(false);
        return dto;
    }

    @Test
    @DisplayName("RN07 / EDP04 - Inserir despesa simples salva com origem MANUAL")
    void inserir_despesaSimples_salvaComSucesso() {
        Conta c = contaAtiva(10L, 1L);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(invocation -> {
            Despesa d = invocation.getArgument(0);
            d.setId(100L);
            return d;
        });

        DespesaFormDTO dto = dtoSimples(10L);
        Despesa salva = despesaService.inserir(dto, 1L, "user_teste", false);

        assertNotNull(salva.getId());
        assertEquals("Energia Elétrica", salva.getNome());
        assertEquals(OrigemLancamento.MANUAL, salva.getOrigem());
        assertEquals(new BigDecimal("150.00"), salva.getValor());
        assertFalse(salva.isParcelada());
        verify(despesaRepository, times(1)).save(any(Despesa.class));
    }

    @Test
    @DisplayName("RN12 - Inserir parcelada gera série com N parcelas e resíduo na última")
    void inserir_despesaParcelada_geraSerieComResiduoNaUltima() {
        Conta c = contaAtiva(10L, 1L);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));

        List<Despesa> salvas = new ArrayList<>();
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(invocation -> {
            Despesa d = invocation.getArgument(0);
            d.setId((long) (salvas.size() + 1));
            salvas.add(d);
            return d;
        });

        DespesaFormDTO dto = dtoSimples(10L);
        dto.setParcelada(true);
        dto.setValorTotalCompra(new BigDecimal("100.00"));
        dto.setQtdParcelas(3);

        despesaService.inserir(dto, 1L, "user_teste", false);

        assertEquals(3, salvas.size());
        Despesa mae = salvas.get(0);
        Despesa p2 = salvas.get(1);
        Despesa p3 = salvas.get(2);

        assertEquals(1, mae.getNroParcela());
        assertNull(mae.getParcelaPai());
        assertEquals(new BigDecimal("33.33"), mae.getValor());

        assertEquals(2, p2.getNroParcela());
        assertEquals(mae, p2.getParcelaPai());
        assertEquals(new BigDecimal("33.33"), p2.getValor());

        assertEquals(3, p3.getNroParcela());
        assertEquals(mae, p3.getParcelaPai());
        assertEquals(new BigDecimal("33.34"), p3.getValor()); // 100.00 - (33.33 * 2) = 33.34
    }

    @Test
    @DisplayName("RN12 / RN16 - Rateio em compra parcelada é replicado com fatias proporcionais")
    void inserir_despesaParceladaComRateio_replicaRateioComResiduo() {
        Conta c = contaAtiva(10L, 1L);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));

        Contato amigo = new Contato();
        amigo.setId(2L);
        when(contatoRepository.findById(2L)).thenReturn(Optional.of(amigo));

        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> {
            Despesa d = inv.getArgument(0);
            d.setId(10L);
            return d;
        });

        DespesaFormDTO dto = dtoSimples(10L);
        dto.setParcelada(true);
        dto.setValorTotalCompra(new BigDecimal("100.00"));
        dto.setQtdParcelas(2);
        dto.setRateio(List.of(new DespesaRateioDTO(2L, "Amigo", new BigDecimal("50.00"), StatusPagamento.NAO, null)));

        despesaService.inserir(dto, 1L, "user_teste", true);

        ArgumentCaptor<DespesaUsuario> captor = ArgumentCaptor.forClass(DespesaUsuario.class);
        verify(despesaUsuarioRepository, times(2)).save(captor.capture());

        List<DespesaUsuario> gravados = captor.getAllValues();
        assertEquals(new BigDecimal("25.00"), gravados.get(0).getValor());
        assertEquals(new BigDecimal("25.00"), gravados.get(1).getValor());
    }

    @Test
    @DisplayName("RN16 / RN17 - Inserir despesa simples com rateio integral para contato (cota do dono zerada) salva com sucesso")
    void inserir_despesaSimplesComRateioIntegralContato_salvaComSucesso() {
        Conta c = contaAtiva(10L, 1L);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));

        Contato contato = new Contato();
        contato.setId(2L);
        contato.setNome("Amigo");
        when(contatoRepository.findById(2L)).thenReturn(Optional.of(contato));

        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> {
            Despesa d = inv.getArgument(0);
            d.setId(100L);
            return d;
        });

        DespesaFormDTO dto = dtoSimples(10L);
        dto.setValor(new BigDecimal("150.00"));
        dto.setRateio(List.of(new DespesaRateioDTO(2L, "Amigo", new BigDecimal("150.00"), StatusPagamento.NAO, null)));

        Despesa salva = despesaService.inserir(dto, 1L, "user_teste", true);

        assertNotNull(salva);
        assertEquals(new BigDecimal("150.00"), salva.getValor());
        assertEquals(1, salva.getRateios().size());

        ArgumentCaptor<DespesaUsuario> captor = ArgumentCaptor.forClass(DespesaUsuario.class);
        verify(despesaUsuarioRepository).save(captor.capture());
        DespesaUsuario rateioSalvo = captor.getValue();
        assertEquals(new BigDecimal("150.00"), rateioSalvo.getValor());
        assertEquals(contato, rateioSalvo.getContato());
    }

    @Test
    @DisplayName("RN08 - Editar despesa não-MANUAL preserva conta/cartão original")
    void editar_despesaImportada_preservaContaOriginal() {
        Conta cOriginal = contaAtiva(10L, 1L);
        Despesa existente = new Despesa();
        existente.setId(50L);
        existente.setNome("Original");
        existente.setValor(new BigDecimal("100.00"));
        existente.setCompetencia(YearMonth.of(2026, 9));
        existente.setConta(cOriginal);
        existente.setOrigem(OrigemLancamento.OPEN_FINANCE);
        existente.setStatusPagamento(StatusPagamento.NAO);

        when(despesaRepository.buscarPorIdEUsuario(50L, 1L)).thenReturn(Optional.of(existente));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaFormDTO dto = dtoSimples(999L);
        dto.setNome("Nome Editado");
        dto.setValor(new BigDecimal("120.00"));

        Despesa editada = despesaService.editar(50L, dto, 1L, "user_teste", false);

        assertEquals("Nome Editado", editada.getNome());
        assertEquals(cOriginal, editada.getConta());
        assertEquals(OrigemLancamento.OPEN_FINANCE, editada.getOrigem());
    }

    @Test
    @DisplayName("RN21 / RN23 - Editar despesa importada do cartão atualiza status de pagamento")
    void editar_despesaImportadaCartao_atualizaStatusPagamento() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(5L);

        Despesa existente = new Despesa();
        existente.setId(60L);
        existente.setCartao(cc);
        existente.setOrigem(OrigemLancamento.IMPORTACAO);
        existente.setStatusPagamento(StatusPagamento.NAO);

        when(despesaRepository.buscarPorIdEUsuario(60L, 1L)).thenReturn(Optional.of(existente));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaFormDTO dto = new DespesaFormDTO();
        dto.setNome("Despesa Importada Paga");
        dto.setValor(new BigDecimal("150.00"));
        dto.setCompetencia("2026-09");
        dto.setDataLancamento(LocalDate.of(2026, 9, 5));
        dto.setStatusPagamento(StatusPagamento.SIM);
        dto.setDataPagamento(LocalDate.of(2026, 9, 10));

        Despesa editada = despesaService.editar(60L, dto, 1L, "user_teste", false);

        assertEquals(StatusPagamento.SIM, editada.getStatusPagamento());
        assertEquals(LocalDate.of(2026, 9, 10), editada.getDataPagamento());
        assertEquals(cc, editada.getCartao());
        assertEquals(OrigemLancamento.IMPORTACAO, editada.getOrigem());
    }

    @Test
    @DisplayName("RN08 / MSG16b - Excluir despesa OPEN_FINANCE lança RegraNegocioException")
    void excluir_despesaOpenFinance_lancaExcecao() {
        Despesa existente = new Despesa();
        existente.setId(50L);
        existente.setOrigem(OrigemLancamento.OPEN_FINANCE);
        when(despesaRepository.buscarPorIdEUsuario(50L, 1L)).thenReturn(Optional.of(existente));

        assertThrows(RegraNegocioException.class, () -> despesaService.excluir(50L, 1L, "user_teste"));
    }

    @Test
    @DisplayName("RN08 - Excluir despesa com origem IMPORTACAO é permitido e realiza soft delete")
    void excluir_despesaImportacaoCartao_permiteExclusao() {
        Despesa d = new Despesa();
        d.setId(51L);
        d.setOrigem(OrigemLancamento.IMPORTACAO);
        when(despesaRepository.buscarPorIdEUsuario(51L, 1L)).thenReturn(Optional.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        despesaService.excluir(51L, 1L, "user_teste");

        assertNotNull(d.getDataExclusao());
        assertEquals("user_teste", d.getExcluidoPor());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN14 - Excluir despesa-mãe de série parcelada exclui todas as parcelas")
    void excluir_despesaMae_excluiTodaSerie() {
        Despesa mae = new Despesa();
        mae.setId(10L);
        mae.setOrigem(OrigemLancamento.MANUAL);
        mae.setParcelada(true);
        mae.setNroParcela(1);
        mae.setParcelaPai(null);

        Despesa f1 = new Despesa();
        f1.setId(11L);
        f1.setParcelaPai(mae);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(mae));
        when(despesaRepository.buscarParcelasDaSerie(10L)).thenReturn(List.of(mae, f1));

        despesaService.excluir(10L, 1L, "user_teste");

        assertNotNull(mae.getDataExclusao());
        assertNotNull(f1.getDataExclusao());
        verify(despesaRepository, times(2)).save(any(Despesa.class));
    }

    @Test
    @DisplayName("RN21 / RN23 - Registrar pagamento em despesa manual de cartão com status NAO_SE_APLICA marca status SIM")
    void registrarPagamento_despesaCartaoManual_marcaPago() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setStatusPagamento(StatusPagamento.NAO_SE_APLICA);
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));

        despesaService.registrarPagamento(10L, LocalDate.of(2026, 9, 10), 1L, "user_teste");

        assertEquals(StatusPagamento.SIM, d.getStatusPagamento());
        assertEquals(LocalDate.of(2026, 9, 10), d.getDataPagamento());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN21 / RN23 - Registrar pagamento em despesa importada de cartão marca status SIM")
    void registrarPagamento_despesaCartaoImportada_marcaPago() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.IMPORTACAO);
        d.setStatusPagamento(StatusPagamento.NAO);
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));

        despesaService.registrarPagamento(10L, LocalDate.of(2026, 9, 10), 1L, "user_teste");

        assertEquals(StatusPagamento.SIM, d.getStatusPagamento());
        assertEquals(LocalDate.of(2026, 9, 10), d.getDataPagamento());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN21 - Registrar pagamento marca status SIM e data")
    void registrarPagamento_despesaConta_marcaPago() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setStatusPagamento(StatusPagamento.NAO);
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));

        despesaService.registrarPagamento(10L, LocalDate.of(2026, 9, 10), 1L, "user_teste");

        assertEquals(StatusPagamento.SIM, d.getStatusPagamento());
        assertEquals(LocalDate.of(2026, 9, 10), d.getDataPagamento());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN21 / C9 - Registrar pagamento em lote com ID de outro usuário lança 404")
    void registrarPagamentoLote_comIdInvalido_lancaExcecao() {
        when(despesaRepository.countPorIdsEUsuario(List.of(1L, 2L), 10L)).thenReturn(1L);

        assertThrows(RegistroNaoEncontradoException.class, () ->
                despesaService.registrarPagamentoLote(List.of(1L, 2L), LocalDate.now(), 10L, "user_teste"));
    }

    @Test
    @DisplayName("RN21 - Registrar pagamento em lote atualiza despesa importada de cartão")
    void registrarPagamentoLote_comDespesaImportada_baixaComSucesso() {
        Despesa d1 = new Despesa();
        d1.setId(1L);
        d1.setStatusPagamento(StatusPagamento.NAO);
        d1.setOrigem(OrigemLancamento.MANUAL);

        Despesa d2 = new Despesa();
        d2.setId(2L);
        d2.setStatusPagamento(StatusPagamento.NAO);
        d2.setOrigem(OrigemLancamento.IMPORTACAO);

        when(despesaRepository.countPorIdsEUsuario(List.of(1L, 2L), 10L)).thenReturn(2L);
        when(despesaRepository.buscarPorIdsEUsuario(List.of(1L, 2L), 10L)).thenReturn(List.of(d1, d2));

        int alteradas = despesaService.registrarPagamentoLote(List.of(1L, 2L), LocalDate.of(2026, 9, 12), 10L, "user_teste");

        assertEquals(2, alteradas);
        assertEquals(StatusPagamento.SIM, d1.getStatusPagamento());
        assertEquals(StatusPagamento.SIM, d2.getStatusPagamento());
        assertEquals(LocalDate.of(2026, 9, 12), d1.getDataPagamento());
        assertEquals(LocalDate.of(2026, 9, 12), d2.getDataPagamento());
        verify(despesaRepository).save(d1);
        verify(despesaRepository).save(d2);
    }

    @Test
    @DisplayName("RN20 - Registrar acerto de rateio atualiza status e data de acerto")
    void registrarAcertoRateio_atualizaCorretamente() {
        Despesa d = new Despesa();
        d.setId(1L);
        when(despesaRepository.buscarPorIdEUsuario(1L, 10L)).thenReturn(Optional.of(d));

        DespesaUsuario du = new DespesaUsuario();
        du.setStatusPagamento(StatusPagamento.NAO);
        du.setDataAcerto(null);
        when(despesaUsuarioRepository.findByDespesaIdAndContatoIdAndDataExclusaoIsNull(1L, 2L)).thenReturn(Optional.of(du));

        despesaService.registrarAcertoRateio(1L, 2L, true, LocalDate.of(2026, 9, 11), 10L, "user_teste");

        assertEquals(StatusPagamento.SIM, du.getStatusPagamento());
        assertEquals(LocalDate.of(2026, 9, 11), du.getDataAcerto());
        verify(despesaUsuarioRepository).save(du);
    }

    @Test
    @DisplayName("EDP10 - Buscar contatos para rateio com termo curto retorna vazio")
    void buscarContatosParaRateio_termoCurto_retornaVazio() {
        List<ContatoRateioDTO> resultado = despesaService.buscarContatosParaRateio("ab", 1L);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(contatoRepository);
    }

    @Test
    @DisplayName("EDP10 - Buscar contatos para rateio com termo válido retorna lista")
    void buscarContatosParaRateio_termoValido_retornaLista() {
        Contato c = new Contato();
        c.setId(5L);
        c.setNome("Carlos Silva");
        c.setTipo(TipoContato.EXTERNO);
        c.setStatus(StatusContato.ATIVO);

        when(contatoRepository.buscarAtivosPorDonoETermo(1L, "carlos")).thenReturn(List.of(c));

        List<ContatoRateioDTO> resultado = despesaService.buscarContatosParaRateio("carlos", 1L);
        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).getId());
        assertEquals("Carlos Silva", resultado.get(0).getNome());
        assertEquals(TipoContato.EXTERNO, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("EDP11 - Cadastrar contato rápido inline salva contato externo")
    void cadastrarContatoRapido_salvaComSucesso() {
        Usuario dono = new Usuario();
        dono.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(dono));

        when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> {
            Contato c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });

        ContatoRapidoDTO dto = new ContatoRapidoDTO("Mariana", "mariana@teste.com", "11999998888", "mariana@pix.com");
        ContatoRateioDTO salvo = despesaService.cadastrarContatoRapido(dto, 1L, "user_teste");

        assertNotNull(salvo.getId());
        assertEquals("Mariana", salvo.getNome());
        assertEquals(TipoContato.EXTERNO, salvo.getTipo());
        assertEquals("mariana@pix.com", salvo.getChavePix());
    }

    @Test
    @DisplayName("EDP10 - Buscar usuários para rateio com termo curto retorna vazio")
    void buscarUsuariosParaRateio_termoCurto_retornaVazio() {
        List<UsuarioRateioDTO> resultado = despesaService.buscarUsuariosParaRateio("ab", 1L);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("RN25 - Inserir despesa recorrente gera série com mesmo valor integral em cada mês")
    void inserir_despesaRecorrente_deveGerarSerieComMesmoValorIntegral() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));

        List<Despesa> salvas = new ArrayList<>();
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> {
            Despesa d = inv.getArgument(0);
            if (d.getId() == null) {
                d.setId((long) (salvas.size() + 1));
            }
            salvas.add(d);
            return d;
        });

        DespesaFormDTO dto = dtoSimples(10L);
        dto.setValor(new BigDecimal("1500.00"));
        dto.setCompetencia("2026-09");
        dto.setRecorrente(true);
        dto.setQtdMesesRecorrencia(12);

        Despesa mae = despesaService.inserir(dto, 1L, "autor", false);

        assertEquals(12, salvas.size());
        assertTrue(mae.isRecorrente());
        assertNull(mae.getRecorrentePai());
        assertEquals(new BigDecimal("1500.00"), mae.getValor());
        assertEquals(YearMonth.of(2026, 9), mae.getCompetencia());

        // Verificar 2ª ocorrência
        Despesa f2 = salvas.get(1);
        assertTrue(f2.isRecorrente());
        assertEquals(mae, f2.getRecorrentePai());
        assertEquals(new BigDecimal("1500.00"), f2.getValor());
        assertEquals(YearMonth.of(2026, 10), f2.getCompetencia());
        assertEquals(StatusPagamento.NAO, f2.getStatusPagamento());

        // Verificar 12ª ocorrência
        Despesa f12 = salvas.get(11);
        assertTrue(f12.isRecorrente());
        assertEquals(mae, f12.getRecorrentePai());
        assertEquals(new BigDecimal("1500.00"), f12.getValor());
        assertEquals(YearMonth.of(2027, 8), f12.getCompetencia());
    }

    @Test
    @DisplayName("RN25 - Excluir despesa-mãe recorrente deve excluir em lote todas as ocorrências")
    void excluir_despesaRecorrenteMae_deveExcluirTodaSerie() {
        Despesa mae = new Despesa();
        mae.setId(100L);
        mae.setOrigem(OrigemLancamento.MANUAL);
        mae.setRecorrente(true);
        mae.setRecorrentePai(null);

        Despesa filha = new Despesa();
        filha.setId(101L);
        filha.setOrigem(OrigemLancamento.MANUAL);
        filha.setRecorrente(true);
        filha.setRecorrentePai(mae);

        when(despesaRepository.buscarPorIdEUsuario(100L, 1L)).thenReturn(Optional.of(mae));
        when(despesaRepository.buscarOcorrenciasRecorrentes(100L)).thenReturn(List.of(mae, filha));

        despesaService.excluir(100L, 1L, "autor");

        assertNotNull(mae.getDataExclusao());
        assertEquals("autor", mae.getExcluidoPor());
        assertNotNull(filha.getDataExclusao());
        assertEquals("autor", filha.getExcluidoPor());
        verify(despesaRepository, times(2)).save(any(Despesa.class));
    }

    @Test
    @DisplayName("RN26 - Duplicar despesa com lista vazia lança RegraNegocioException")
    void duplicar_comListaVazia_deveLancarExcecao() {
        assertThrows(RegraNegocioException.class, () -> despesaService.duplicar(List.of(), null, 1L, "autor"));
    }

    @Test
    @DisplayName("RN26 - Duplicar despesas com sucesso para competência destino")
    void duplicar_comSucesso_paraCompetenciaDestino() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setNome("Supermercado");
        d.setDescricao("Compras do mês");
        d.setValor(new BigDecimal("350.00"));
        d.setDataLancamento(LocalDate.of(2026, 9, 1));
        d.setDataVencimento(LocalDate.of(2026, 9, 10));
        d.setCompetencia(YearMonth.of(2026, 9));
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setStatusPagamento(StatusPagamento.NAO);

        when(despesaRepository.countPorIdsEUsuario(List.of(10L), 1L)).thenReturn(1L);
        when(despesaRepository.buscarPorIdsEUsuario(List.of(10L), 1L)).thenReturn(List.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> duplicadas = despesaService.duplicar(List.of(10L), "2026-10", 1L, "autor");

        assertEquals(1, duplicadas.size());
        Despesa copia = duplicadas.get(0);
        assertEquals(d.getNome(), copia.getNome());
        assertEquals(d.getValor(), copia.getValor());
        assertEquals(YearMonth.of(2026, 10), copia.getCompetencia());
        assertEquals(LocalDate.of(2026, 10, 10), copia.getDataVencimento());
        assertEquals(StatusPagamento.NAO, copia.getStatusPagamento());
        assertNull(copia.getDataPagamento());
        assertEquals(OrigemLancamento.MANUAL, copia.getOrigem());
        assertFalse(copia.isParcelada());
        assertFalse(copia.isRecorrente());
    }

    @Test
    @DisplayName("RN35 - Atualizar nome com valor nulo ou em branco lança RegraNegocioException")
    void atualizarNome_comNomeInvalido_deveLancarExcecao() {
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarNome(10L, null, 1L, "autor"));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarNome(10L, "", 1L, "autor"));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarNome(10L, "   ", 1L, "autor"));
    }

    @Test
    @DisplayName("RN35 - Atualizar nome com sucesso")
    void atualizarNome_comSucesso_deveAtualizar() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setNome("Nome Antigo");
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa atualizada = despesaService.atualizarNome(10L, "  Novo Nome  ", 1L, "autor");

        assertEquals("Novo Nome", atualizada.getNome());
        assertEquals("autor", atualizada.getAlteradoPor());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN27 - Atualizar valor com valor menor ou igual a zero lança RegraNegocioException")
    void atualizarValor_comValorInvalido_deveLancarExcecao() {
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarValor(10L, BigDecimal.ZERO, 1L, "autor"));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarValor(10L, new BigDecimal("-10.00"), 1L, "autor"));
    }

    @Test
    @DisplayName("RN27 - Atualizar valor com sucesso")
    void atualizarValor_comSucesso_deveAtualizar() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setValor(new BigDecimal("100.00"));
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa atualizada = despesaService.atualizarValor(10L, new BigDecimal("150.00"), 1L, "autor");

        assertEquals(new BigDecimal("150.00"), atualizada.getValor());
        assertEquals("autor", atualizada.getAlteradoPor());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN32 - Atualizar competência com formato inválido lança RegraNegocioException")
    void atualizarCompetencia_comCompetenciaInvalida_deveLancarExcecao() {
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCompetencia(10L, null, 1L, "autor"));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCompetencia(10L, "", 1L, "autor"));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCompetencia(10L, "invalido", 1L, "autor"));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCompetencia(10L, "2026/08", 1L, "autor"));
    }

    @Test
    @DisplayName("RN32 - Atualizar competência com sucesso")
    void atualizarCompetencia_comSucesso_deveAtualizar() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setCompetencia(YearMonth.of(2026, 8));
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa atualizada = despesaService.atualizarCompetencia(10L, "2026-10", 1L, "autor");

        assertEquals(YearMonth.of(2026, 10), atualizada.getCompetencia());
        assertEquals("autor", atualizada.getAlteradoPor());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN33 - Atualizar categoria com sucesso vincula categoria à despesa")
    void atualizarCategoria_comCategoriaValida_deveAtualizar() {
        Despesa d = new Despesa();
        d.setId(10L);

        Categoria cat = new Categoria();
        cat.setId(2L);
        cat.setNome("Alimentação");
        cat.setAtivo(true);
        cat.setAplicaA(AplicaA.DESPESA);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(2L)).thenReturn(Optional.of(cat));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa atualizada = despesaService.atualizarCategoria(10L, 2L, 1L, "autor");

        assertEquals(cat, atualizada.getCategoria());
        assertEquals("autor", atualizada.getAlteradoPor());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN33 - Atualizar categoria com categoriaId nulo desvincula a categoria")
    void atualizarCategoria_comCategoriaNull_deveDesvincular() {
        Categoria cat = new Categoria();
        cat.setId(2L);

        Despesa d = new Despesa();
        d.setId(10L);
        d.setCategoria(cat);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa atualizada = despesaService.atualizarCategoria(10L, null, 1L, "autor");

        assertNull(atualizada.getCategoria());
        assertEquals("autor", atualizada.getAlteradoPor());
        verify(despesaRepository).save(d);
    }

    @Test
    @DisplayName("RN33 - Atualizar categoria inexistente, inativa ou que não se aplica a despesa lança RegraNegocioException")
    void atualizarCategoria_invalida_deveLancarExcecao() {
        Despesa d = new Despesa();
        d.setId(10L);
        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));

        // Categoria não encontrada
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCategoria(10L, 99L, 1L, "autor"));

        // Categoria inativa
        Categoria catInativa = new Categoria();
        catInativa.setId(3L);
        catInativa.setAtivo(false);
        catInativa.setAplicaA(AplicaA.DESPESA);
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(3L)).thenReturn(Optional.of(catInativa));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCategoria(10L, 3L, 1L, "autor"));

        // Categoria que aplica apenas a RECEITA
        Categoria catReceita = new Categoria();
        catReceita.setId(4L);
        catReceita.setAtivo(true);
        catReceita.setAplicaA(AplicaA.RECEITA);
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(4L)).thenReturn(Optional.of(catReceita));
        assertThrows(RegraNegocioException.class, () -> despesaService.atualizarCategoria(10L, 4L, 1L, "autor"));
    }

    @Test
    @DisplayName("RN34 - Atualizar forma de pagamento para CARTAO altera cartão, meio para CREDITO e status para NAO_SE_APLICA")
    void atualizarFormaPagamento_paraCartao_sucesso() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setConta(contaAtiva(1L, 1L));
        d.setStatusPagamento(StatusPagamento.NAO);

        CartaoCredito cc = cartaoAtivo(5L, 1L);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(5L, 1L)).thenReturn(Optional.of(cc));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa res = despesaService.atualizarFormaPagamento(10L, "CARTAO", 5L, null, null, 1L, "autor");

        assertEquals(cc, res.getCartao());
        assertNull(res.getConta());
        assertEquals(MeioPagamento.CREDITO, res.getMeioPagamento());
        assertEquals(StatusPagamento.NAO, res.getStatusPagamento());
        assertNull(res.getDataPagamento());
        assertEquals("autor", res.getAlteradoPor());
    }

    @Test
    @DisplayName("RN34 - Atualizar forma de pagamento para CONTA com meio PIX altera conta e meio de pagamento")
    void atualizarFormaPagamento_paraConta_sucesso() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setCartao(cartaoAtivo(5L, 1L));
        d.setStatusPagamento(StatusPagamento.NAO_SE_APLICA);

        Conta c = contaAtiva(2L, 1L);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(2L, 1L)).thenReturn(Optional.of(c));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa res = despesaService.atualizarFormaPagamento(10L, "CONTA", null, 2L, MeioPagamento.PIX, 1L, "autor");

        assertEquals(c, res.getConta());
        assertNull(res.getCartao());
        assertEquals(MeioPagamento.PIX, res.getMeioPagamento());
        assertEquals(StatusPagamento.NAO, res.getStatusPagamento());
        assertEquals("autor", res.getAlteradoPor());
    }

    @Test
    @DisplayName("RN34 - Atualizar forma de pagamento para DINHEIRO altera para conta CARTEIRA e meio DINHEIRO")
    void atualizarFormaPagamento_paraDinheiro_sucesso() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setCartao(cartaoAtivo(5L, 1L));
        d.setStatusPagamento(StatusPagamento.NAO_SE_APLICA);

        Conta carteira = new Conta();
        carteira.setId(3L);
        carteira.setDescricao("Carteira");
        carteira.setTipo(TipoConta.CARTEIRA);
        carteira.setAtivo(true);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(3L, 1L)).thenReturn(Optional.of(carteira));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        Despesa res = despesaService.atualizarFormaPagamento(10L, "DINHEIRO", null, 3L, null, 1L, "autor");

        assertEquals(carteira, res.getConta());
        assertNull(res.getCartao());
        assertEquals(MeioPagamento.DINHEIRO, res.getMeioPagamento());
        assertEquals(StatusPagamento.NAO, res.getStatusPagamento());
        assertEquals("autor", res.getAlteradoPor());
    }

    @Test
    @DisplayName("RN34 - Atualizar forma de pagamento de despesa OPEN_FINANCE lança RegraNegocioException")
    void atualizarFormaPagamento_openFinance_lancaExcecao() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.OPEN_FINANCE);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));

        assertThrows(RegraNegocioException.class, () ->
                despesaService.atualizarFormaPagamento(10L, "CARTAO", 5L, null, null, 1L, "autor"));
    }

    @Test
    @DisplayName("RN34 - Atualizar forma de pagamento com cartão inválido lança RegraNegocioException")
    void atualizarFormaPagamento_cartaoInvalido_lancaExcecao() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setOrigem(OrigemLancamento.MANUAL);

        when(despesaRepository.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(d));
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () ->
                despesaService.atualizarFormaPagamento(10L, "CARTAO", 99L, null, null, 1L, "autor"));
    }

    @Test
    @DisplayName("DespesaGridDTO - Despesa sem rateio tem valorUsuario igual ao valor total e valorRateado zero")
    void despesaGridDTO_semRateio_devePreencherValorUsuarioComValorTotal() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setValor(new BigDecimal("100.00"));

        DespesaGridDTO dto = new DespesaGridDTO(d);

        assertEquals(new BigDecimal("100.00"), dto.getValor());
        assertEquals(new BigDecimal("100.00"), dto.getValorUsuario());
        assertEquals(BigDecimal.ZERO, dto.getValorRateado());
        assertFalse(dto.isTemRateio());
    }

    @Test
    @DisplayName("DespesaGridDTO - Despesa com rateio parcial deduz as fatias dos contatos da cota do titular")
    void despesaGridDTO_comRateioParcial_deveDeduzirFatiaDosContatos() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setValor(new BigDecimal("100.00"));

        DespesaUsuario du = new DespesaUsuario();
        du.setValor(new BigDecimal("60.00"));
        d.getRateios().add(du);

        DespesaGridDTO dto = new DespesaGridDTO(d);

        assertEquals(new BigDecimal("100.00"), dto.getValor());
        assertEquals(new BigDecimal("40.00"), dto.getValorUsuario());
        assertEquals(new BigDecimal("60.00"), dto.getValorRateado());
        assertTrue(dto.isTemRateio());
    }

    @Test
    @DisplayName("DespesaGridDTO - Despesa com rateio integral cota zerada atribui valorUsuario zero e valorRateado total")
    void despesaGridDTO_comRateioIntegral_deveZerarValorUsuario() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setValor(new BigDecimal("100.00"));

        DespesaUsuario du = new DespesaUsuario();
        du.setValor(new BigDecimal("100.00"));
        d.getRateios().add(du);

        DespesaGridDTO dto = new DespesaGridDTO(d);

        assertEquals(new BigDecimal("100.00"), dto.getValor());
        assertEquals(new BigDecimal("0.00"), dto.getValorUsuario());
        assertEquals(new BigDecimal("100.00"), dto.getValorRateado());
        assertTrue(dto.isTemRateio());
    }

    @Test
    @DisplayName("RN26 - Duplicar despesa como parcelada (base PARCELA) gera série com rateio clonado")
    void duplicar_comoParcelada_comBaseParcelaERateio_deveGerarSerieEClonarRateio() {
        Contato contato = new Contato();
        contato.setId(2L);
        contato.setNome("Contato Rateio");

        Despesa d = new Despesa();
        d.setId(10L);
        d.setNome("Compra Loja");
        d.setValor(new BigDecimal("50.00"));
        d.setDataLancamento(LocalDate.of(2026, 9, 1));
        d.setDataVencimento(LocalDate.of(2026, 9, 10));
        d.setCompetencia(YearMonth.of(2026, 9));
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setStatusPagamento(StatusPagamento.NAO);

        DespesaUsuario du = new DespesaUsuario();
        du.setId(101L);
        du.setDespesa(d);
        du.setContato(contato);
        du.setValor(new BigDecimal("20.00"));
        du.setStatusPagamento(StatusPagamento.NAO);
        d.getRateios().add(du);

        when(despesaRepository.countPorIdsEUsuario(List.of(10L), 1L)).thenReturn(1L);
        when(despesaRepository.buscarPorIdsEUsuario(List.of(10L), 1L)).thenReturn(List.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> duplicadas = despesaService.duplicar(
                List.of(10L), "2026-10", "PARCELADA", 3, "PARCELA", null, 1L, "autor");

        assertEquals(3, duplicadas.size());

        Despesa p1 = duplicadas.get(0);
        assertTrue(p1.isParcelada());
        assertEquals(1, p1.getNroParcela());
        assertEquals(3, p1.getQtdParcelas());
        assertNull(p1.getParcelaPai());
        assertEquals(new BigDecimal("50.00"), p1.getValor());
        assertEquals(new BigDecimal("150.00"), p1.getValorTotalCompra());
        assertEquals(YearMonth.of(2026, 10), p1.getCompetencia());
        assertEquals(LocalDate.of(2026, 10, 10), p1.getDataVencimento());

        Despesa p2 = duplicadas.get(1);
        assertTrue(p2.isParcelada());
        assertEquals(2, p2.getNroParcela());
        assertEquals(3, p2.getQtdParcelas());
        assertEquals(p1, p2.getParcelaPai());
        assertEquals(new BigDecimal("50.00"), p2.getValor());
        assertEquals(YearMonth.of(2026, 11), p2.getCompetencia());
        assertEquals(LocalDate.of(2026, 11, 10), p2.getDataVencimento());

        Despesa p3 = duplicadas.get(2);
        assertTrue(p3.isParcelada());
        assertEquals(3, p3.getNroParcela());
        assertEquals(p1, p3.getParcelaPai());
        assertEquals(new BigDecimal("50.00"), p3.getValor());
        assertEquals(YearMonth.of(2026, 12), p3.getCompetencia());

        verify(despesaUsuarioRepository, times(3)).save(any(DespesaUsuario.class));
    }

    @Test
    @DisplayName("RN26 - Duplicar despesa como parcelada (base TOTAL) divide valor entre parcelas")
    void duplicar_comoParcelada_comBaseTotal_deveDividirValor() {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setNome("Compra Total");
        d.setValor(new BigDecimal("100.00"));
        d.setCompetencia(YearMonth.of(2026, 9));
        d.setDataLancamento(LocalDate.of(2026, 9, 1));
        d.setDataVencimento(LocalDate.of(2026, 9, 10));

        when(despesaRepository.countPorIdsEUsuario(List.of(10L), 1L)).thenReturn(1L);
        when(despesaRepository.buscarPorIdsEUsuario(List.of(10L), 1L)).thenReturn(List.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> duplicadas = despesaService.duplicar(
                List.of(10L), "2026-10", "PARCELADA", 3, "TOTAL", null, 1L, "autor");

        assertEquals(3, duplicadas.size());
        assertEquals(new BigDecimal("33.33"), duplicadas.get(0).getValor());
        assertEquals(new BigDecimal("33.33"), duplicadas.get(1).getValor());
        assertEquals(new BigDecimal("33.34"), duplicadas.get(2).getValor());
        assertEquals(new BigDecimal("100.00"), duplicadas.get(0).getValorTotalCompra());
    }

    @Test
    @DisplayName("RN26 - Duplicar despesa como recorrente gera série com rateio clonado")
    void duplicar_comoRecorrente_comRateio_deveGerarSerieEClonarRateio() {
        Contato contato = new Contato();
        contato.setId(3L);
        contato.setNome("Contato Recorrente");

        Despesa d = new Despesa();
        d.setId(20L);
        d.setNome("Assinatura");
        d.setValor(new BigDecimal("80.00"));
        d.setDataLancamento(LocalDate.of(2026, 9, 5));
        d.setDataVencimento(LocalDate.of(2026, 9, 15));
        d.setCompetencia(YearMonth.of(2026, 9));

        DespesaUsuario du = new DespesaUsuario();
        du.setId(201L);
        du.setDespesa(d);
        du.setContato(contato);
        du.setValor(new BigDecimal("30.00"));
        d.getRateios().add(du);

        when(despesaRepository.countPorIdsEUsuario(List.of(20L), 1L)).thenReturn(1L);
        when(despesaRepository.buscarPorIdsEUsuario(List.of(20L), 1L)).thenReturn(List.of(d));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> duplicadas = despesaService.duplicar(
                List.of(20L), "2026-10", "RECORRENTE", null, "PARCELA", 4, 1L, "autor");

        assertEquals(4, duplicadas.size());

        Despesa r1 = duplicadas.get(0);
        assertTrue(r1.isRecorrente());
        assertNull(r1.getRecorrentePai());
        assertEquals(new BigDecimal("80.00"), r1.getValor());
        assertEquals(YearMonth.of(2026, 10), r1.getCompetencia());

        Despesa r2 = duplicadas.get(1);
        assertTrue(r2.isRecorrente());
        assertEquals(r1, r2.getRecorrentePai());
        assertEquals(new BigDecimal("80.00"), r2.getValor());
        assertEquals(YearMonth.of(2026, 11), r2.getCompetencia());

        Despesa r4 = duplicadas.get(3);
        assertTrue(r4.isRecorrente());
        assertEquals(r1, r4.getRecorrentePai());
        assertEquals(YearMonth.of(2027, 1), r4.getCompetencia());

        verify(despesaUsuarioRepository, times(4)).save(any(DespesaUsuario.class));
    }

    @Test
    @DisplayName("RN26 - Editar despesa avulsa convertendo para parcelada gera parcelas filhas e rateio")
    void editar_converterAvulsaParaParcelada_deveGerarParcelasFilhasERateio() {
        Contato contato = new Contato();
        contato.setId(2L);
        when(contatoRepository.findById(2L)).thenReturn(Optional.of(contato));

        Despesa existente = new Despesa();
        existente.setId(50L);
        existente.setNome("Compra Avulsa");
        existente.setValor(new BigDecimal("100.00"));
        existente.setCompetencia(YearMonth.of(2026, 9));
        existente.setDataLancamento(LocalDate.of(2026, 9, 1));
        existente.setDataVencimento(LocalDate.of(2026, 9, 10));
        existente.setParcelada(false);
        existente.setRecorrente(false);
        existente.setOrigem(OrigemLancamento.MANUAL);

        when(despesaRepository.buscarPorIdEUsuario(50L, 1L)).thenReturn(Optional.of(existente));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaFormDTO dto = new DespesaFormDTO();
        dto.setNome("Compra Avulsa Editada");
        dto.setCompetencia("2026-09");
        dto.setDataLancamento(LocalDate.of(2026, 9, 1));
        dto.setDataVencimento(LocalDate.of(2026, 9, 10));
        dto.setParcelada(true);
        dto.setQtdParcelas(3);
        dto.setValorTotalCompra(new BigDecimal("300.00"));
        dto.setValor(new BigDecimal("100.00"));
        dto.setRateio(List.of(new DespesaRateioDTO(2L, "Contato", new BigDecimal("60.00"), StatusPagamento.NAO, null)));

        Despesa editada = despesaService.editar(50L, dto, 1L, "autor", true);

        assertTrue(editada.isParcelada());
        assertEquals(1, editada.getNroParcela());
        assertEquals(3, editada.getQtdParcelas());
        assertNull(editada.getParcelaPai());
        assertEquals(new BigDecimal("100.00"), editada.getValor());
        assertEquals(new BigDecimal("300.00"), editada.getValorTotalCompra());

        // Deve salvar a mãe (existente) + 2 filhas = 3 chamadas a despesaRepository.save
        verify(despesaRepository, times(3)).save(any(Despesa.class));
        // Para cada parcela filha (2 filhas), rateio é salvo com fatia proporcional (60 / 3 = 20)
        verify(despesaUsuarioRepository, atLeast(2)).save(any(DespesaUsuario.class));
    }

    @Test
    @DisplayName("RN26 - Editar despesa avulsa convertendo para recorrente gera ocorrências filhas e rateio")
    void editar_converterAvulsaParaRecorrente_deveGerarRecorrenciasFilhasERateio() {
        Contato contato = new Contato();
        contato.setId(2L);
        when(contatoRepository.findById(2L)).thenReturn(Optional.of(contato));

        Despesa existente = new Despesa();
        existente.setId(50L);
        existente.setNome("Mensalidade");
        existente.setValor(new BigDecimal("120.00"));
        existente.setCompetencia(YearMonth.of(2026, 9));
        existente.setDataLancamento(LocalDate.of(2026, 9, 1));
        existente.setDataVencimento(LocalDate.of(2026, 9, 10));
        existente.setParcelada(false);
        existente.setRecorrente(false);
        existente.setOrigem(OrigemLancamento.MANUAL);

        when(despesaRepository.buscarPorIdEUsuario(50L, 1L)).thenReturn(Optional.of(existente));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaFormDTO dto = new DespesaFormDTO();
        dto.setNome("Mensalidade Recorrente");
        dto.setCompetencia("2026-09");
        dto.setDataLancamento(LocalDate.of(2026, 9, 1));
        dto.setDataVencimento(LocalDate.of(2026, 9, 10));
        dto.setRecorrente(true);
        dto.setQtdMesesRecorrencia(3);
        dto.setValor(new BigDecimal("120.00"));
        dto.setRateio(List.of(new DespesaRateioDTO(2L, "Contato", new BigDecimal("40.00"), StatusPagamento.NAO, null)));

        Despesa editada = despesaService.editar(50L, dto, 1L, "autor", true);

        assertTrue(editada.isRecorrente());
        assertNull(editada.getRecorrentePai());
        assertEquals(new BigDecimal("120.00"), editada.getValor());

        // Deve salvar a geradora + 2 filhas = 3 chamadas a despesaRepository.save
        verify(despesaRepository, times(3)).save(any(Despesa.class));
        // Para cada filha (2 filhas), rateio é salvo com fatia integral (40)
        verify(despesaUsuarioRepository, atLeast(2)).save(any(DespesaUsuario.class));
    }
}
