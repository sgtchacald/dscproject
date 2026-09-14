package br.com.diegocordeiro.dscproject.service.receita;

import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaFormDTO;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaGridDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.receita.Receita;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceitaServiceTest {

    @Mock
    private ReceitaRepository receitaRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private ReceitaService receitaService;

    @BeforeEach
    void setUp() {
        receitaService = new ReceitaService(receitaRepository, contaRepository, categoriaRepository);
    }

    private Conta contaAtiva(Long id, Long usuarioId) {
        Conta c = new Conta();
        c.setId(id);
        c.setDescricao("Nubank Conta Corrente");
        c.setAtivo(true);
        var usuario = new br.com.diegocordeiro.dscproject.model.usuario.Usuario();
        usuario.setId(usuarioId);
        c.setUsuario(usuario);
        return c;
    }

    private ReceitaFormDTO dtoValido() {
        ReceitaFormDTO dto = new ReceitaFormDTO();
        dto.setNome("Salário");
        dto.setValor(new BigDecimal("5000.00"));
        dto.setDataLancamento(LocalDate.of(2026, 9, 5));
        dto.setCompetencia("2026-09");
        dto.setContaId(10L);
        dto.setRecebido(false);
        return dto;
    }

    private Receita criarReceita(Long id, Long contaUsuarioId, boolean recebido, boolean excluida) {
        Conta conta = new Conta();
        conta.setId(10L);
        conta.setDescricao("Nubank Conta Corrente");
        var usuario = new br.com.diegocordeiro.dscproject.model.usuario.Usuario();
        usuario.setId(contaUsuarioId);
        conta.setUsuario(usuario);

        Categoria categoria = new Categoria();
        categoria.setId(20L);
        categoria.setNome("Salário");

        Receita r = new Receita();
        r.setId(id);
        r.setNome("Salário");
        r.setDescricao(null);
        r.setValor(new BigDecimal("5000.00"));
        r.setDataLancamento(LocalDate.of(2026, 9, 5));
        r.setCompetencia(YearMonth.of(2026, 9));
        r.setConta(conta);
        r.setCategoria(categoria);
        r.setOrigem(OrigemLancamento.MANUAL);
        r.setRecebido(recebido);
        if (recebido) {
            r.setDataRecebimento(LocalDate.of(2026, 9, 6));
        }
        if (excluida) {
            r.setDataExclusao(java.time.Instant.now());
            r.setExcluidoPor("user1");
        }
        return r;
    }

    @Test
    @DisplayName("RF01 / RF07 / RN02 - Lista as receitas apenas do usuário autenticado, mapeando todos os campos do grid")
    void listarParaGrid_deveConsultarPorUsuarioEMapearCampos() {
        Receita receita = criarReceita(1L, 1L, false, false);
        when(receitaRepository.listarPorUsuario(1L)).thenReturn(List.of(receita));

        List<ReceitaGridDTO> grid = receitaService.listarParaGrid(1L);

        assertEquals(1, grid.size());
        ReceitaGridDTO dto = grid.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(YearMonth.of(2026, 9), dto.getCompetencia());
        assertEquals("Salário", dto.getNome());
        assertEquals(new BigDecimal("5000.00"), dto.getValor());
        assertEquals(LocalDate.of(2026, 9, 5), dto.getDataLancamento());
        assertFalse(dto.isRecebido());
        assertNull(dto.getDataRecebimento());
        assertEquals(OrigemLancamento.MANUAL, dto.getOrigem());
        assertEquals(10L, dto.getContaId());
        assertEquals("Nubank Conta Corrente", dto.getContaDescricao());
        assertEquals(20L, dto.getCategoriaId());
        assertEquals("Salário", dto.getCategoriaNome());
        assertFalse(dto.isExcluido());

        verify(receitaRepository).listarPorUsuario(1L);
    }

    @Test
    @DisplayName("QUADRO_DESCRITIVO_1/ID13 - Receita recebida aparece com dataRecebimento preenchida")
    void listarParaGrid_receitaRecebida_devePreencherDataRecebimento() {
        Receita receita = criarReceita(2L, 1L, true, false);
        when(receitaRepository.listarPorUsuario(1L)).thenReturn(List.of(receita));

        ReceitaGridDTO dto = receitaService.listarParaGrid(1L).get(0);

        assertTrue(dto.isRecebido());
        assertEquals(LocalDate.of(2026, 9, 6), dto.getDataRecebimento());
    }

    @Test
    @DisplayName("QUADRO_DESCRITIVO_1/ID13 - Receita excluída continua na listagem, sinalizada como excluída (C1 não filtra soft delete)")
    void listarParaGrid_receitaExcluida_apareceNaListaSinalizada() {
        Receita receita = criarReceita(3L, 1L, false, true);
        when(receitaRepository.listarPorUsuario(1L)).thenReturn(List.of(receita));

        ReceitaGridDTO dto = receitaService.listarParaGrid(1L).get(0);

        assertTrue(dto.isExcluido());
    }

    @Test
    @DisplayName("Receita sem categoria vem com categoria nula no grid")
    void listarParaGrid_semCategoria_deveVirComCategoriaNula() {
        Receita receita = criarReceita(4L, 1L, false, false);
        receita.setCategoria(null);
        when(receitaRepository.listarPorUsuario(1L)).thenReturn(List.of(receita));

        ReceitaGridDTO dto = receitaService.listarParaGrid(1L).get(0);

        assertNull(dto.getCategoriaId());
        assertNull(dto.getCategoriaNome());
    }

    @Test
    @DisplayName("RN03/RN07 - Cadastra receita fixando origem MANUAL, mesmo sem o DTO ter esse campo")
    void inserir_comDadosValidos_deveSalvarComOrigemManual() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Receita salva = receitaService.inserir(dtoValido(), 1L, "user1");

        assertEquals("Salário", salva.getNome());
        assertEquals(new BigDecimal("5000.00"), salva.getValor());
        assertEquals(YearMonth.of(2026, 9), salva.getCompetencia());
        assertEquals(OrigemLancamento.MANUAL, salva.getOrigem());
        assertEquals(10L, salva.getConta().getId());
        assertFalse(salva.isRecebido());
        assertNull(salva.getDataRecebimento());
        verify(receitaRepository).save(any(Receita.class));
    }

    @Test
    @DisplayName("RN03/C3 - Inserir com conta inválida lança RegraNegocioException e não persiste")
    void inserir_comContaInvalida_deveLancarExcecaoENaoSalvar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.empty());

        ReceitaFormDTO dto = dtoValido();
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> receitaService.inserir(dto, 1L, "user1"));
        assertEquals("contaId", ex.getCampo());
        assertEquals("msg.receita.conta.invalida", ex.getMessage());
        verify(receitaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN06 - Cadastrar já recebida grava a data de recebimento")
    void inserir_recebidaComData_deveGravarDataRecebimento() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaFormDTO dto = dtoValido();
        dto.setRecebido(true);
        dto.setDataRecebimento(LocalDate.of(2026, 9, 6));

        Receita salva = receitaService.inserir(dto, 1L, "user1");

        assertTrue(salva.isRecebido());
        assertEquals(LocalDate.of(2026, 9, 6), salva.getDataRecebimento());
    }

    @Test
    @DisplayName("RN06 - Cadastrar como não recebida sempre limpa a data de recebimento, mesmo se enviada")
    void inserir_naoRecebida_deveLimparDataRecebimentoEnviada() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaFormDTO dto = dtoValido();
        dto.setRecebido(false);
        dto.setDataRecebimento(LocalDate.of(2026, 9, 6)); // não deveria sobreviver

        Receita salva = receitaService.inserir(dto, 1L, "user1");

        assertFalse(salva.isRecebido());
        assertNull(salva.getDataRecebimento());
    }

    @Test
    @DisplayName("C4 - Cadastrar com categoria válida vincula a categoria")
    void inserir_comCategoriaValida_deveVincularCategoria() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));
        Categoria categoria = new Categoria();
        categoria.setId(20L);
        categoria.setAtivo(true);
        categoria.setAplicaA(AplicaA.RECEITA);
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(20L)).thenReturn(Optional.of(categoria));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaFormDTO dto = dtoValido();
        dto.setCategoriaId(20L);

        Receita salva = receitaService.inserir(dto, 1L, "user1");

        assertEquals(20L, salva.getCategoria().getId());
    }

    @Test
    @DisplayName("C4 - Cadastrar com categoria inválida lança RegraNegocioException e não persiste")
    void inserir_comCategoriaInvalida_deveLancarExcecaoENaoSalvar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(20L)).thenReturn(Optional.empty());

        ReceitaFormDTO dto = dtoValido();
        dto.setCategoriaId(20L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> receitaService.inserir(dto, 1L, "user1"));
        assertEquals("categoriaId", ex.getCampo());
        verify(receitaRepository, never()).save(any());
    }

    @Test
    @DisplayName("BDD 16.3 - Buscar receita de outro usuário lança RegistroNaoEncontradoException (404)")
    void buscarParaEdicao_quandoReceitaDeOutroUsuario_deveLancarRegistroNaoEncontradoException() {
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(70L, 1L)).thenReturn(Optional.empty());

        assertThrows(br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException.class,
            () -> receitaService.buscarParaEdicao(70L, 1L));
    }

    @Test
    @DisplayName("EDP03 - Buscar receita do próprio usuário mapeia todos os campos de edição")
    void buscarParaEdicao_quandoReceitaDoUsuario_deveMapearCampos() {
        Receita receita = criarReceita(5L, 1L, true, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(5L, 1L)).thenReturn(Optional.of(receita));

        var dto = receitaService.buscarParaEdicao(5L, 1L);

        assertEquals(5L, dto.getId());
        assertEquals(YearMonth.of(2026, 9), dto.getCompetencia());
        assertEquals("Salário", dto.getNome());
        assertTrue(dto.isRecebido());
        assertEquals(10L, dto.getContaId());
        assertEquals(20L, dto.getCategoriaId());
        assertEquals(OrigemLancamento.MANUAL, dto.getOrigem());
    }

    @Test
    @DisplayName("BDD 16.4 - Editar receita de outro usuário lança RegistroNaoEncontradoException (404) e não altera nada")
    void editar_quandoReceitaDeOutroUsuario_deveLancarExcecaoENaoSalvar() {
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(70L, 1L)).thenReturn(Optional.empty());

        ReceitaFormDTO dto = dtoValido();
        assertThrows(br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException.class,
            () -> receitaService.editar(70L, dto, 1L, "user1"));
        verify(receitaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN02/RN10 - Editar receita MANUAL do próprio usuário atualiza os campos e revalida a conta")
    void editar_receitaManualComDadosValidos_deveAtualizarCampos() {
        Receita existente = criarReceita(5L, 1L, false, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(5L, 1L)).thenReturn(Optional.of(existente));

        Conta novaConta = contaAtiva(11L, 1L);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(11L, 1L)).thenReturn(Optional.of(novaConta));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaFormDTO dto = dtoValido();
        dto.setId(5L);
        dto.setContaId(11L);
        dto.setNome("Salário Atualizado");

        Receita editada = receitaService.editar(5L, dto, 1L, "user1");

        assertEquals("Salário Atualizado", editada.getNome());
        assertEquals(11L, editada.getConta().getId());
        verify(receitaRepository).save(existente);
    }

    @Test
    @DisplayName("RN08 - Editar receita do Open Finance ignora a conta enviada e preserva a original")
    void editar_receitaOpenFinance_devePreservarContaEOrigemOriginais() {
        Receita existente = criarReceita(6L, 1L, false, false);
        existente.setOrigem(OrigemLancamento.OPEN_FINANCE);
        Long contaOriginalId = existente.getConta().getId();
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(6L, 1L)).thenReturn(Optional.of(existente));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaFormDTO dto = dtoValido();
        dto.setId(6L);
        dto.setContaId(999L); // deve ser ignorado
        dto.setNome("PIX recebido - categorizado");

        Receita editada = receitaService.editar(6L, dto, 1L, "user1");

        assertEquals(contaOriginalId, editada.getConta().getId());
        assertEquals(OrigemLancamento.OPEN_FINANCE, editada.getOrigem());
        assertEquals("PIX recebido - categorizado", editada.getNome());
        verify(contaRepository, never()).findByIdAndUsuarioIdAndDataExclusaoIsNull(999L, 1L);
    }

    @Test
    @DisplayName("RN06 - Editar desmarcando recebido limpa a data de recebimento")
    void editar_desmarcandoRecebido_deveLimparDataRecebimento() {
        Receita existente = criarReceita(7L, 1L, true, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(7L, 1L)).thenReturn(Optional.of(existente));
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaFormDTO dto = dtoValido();
        dto.setId(7L);
        dto.setRecebido(false);

        Receita editada = receitaService.editar(7L, dto, 1L, "user1");

        assertFalse(editada.isRecebido());
        assertNull(editada.getDataRecebimento());
    }

    @Test
    @DisplayName("BDD 16.4 - Registrar recebimento de receita de outro usuário lança RegistroNaoEncontradoException (404)")
    void marcarRecebida_quandoReceitaDeOutroUsuario_deveLancarExcecao() {
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(70L, 1L)).thenReturn(Optional.empty());

        assertThrows(br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException.class,
            () -> receitaService.marcarRecebida(70L, LocalDate.of(2026, 10, 5), 1L, "user1"));
        verify(receitaRepository, never()).save(any());
    }

    @Test
    @DisplayName("BDD 16.10 - Registrar recebimento grava a data e marca como recebida")
    void marcarRecebida_deveGravarDataEMarcarRecebida() {
        Receita receita = criarReceita(8L, 1L, false, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(8L, 1L)).thenReturn(Optional.of(receita));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Receita marcada = receitaService.marcarRecebida(8L, LocalDate.of(2026, 10, 5), 1L, "user1");

        assertTrue(marcada.isRecebido());
        assertEquals(LocalDate.of(2026, 10, 5), marcada.getDataRecebimento());
        verify(receitaRepository).save(receita);
    }

    @Test
    @DisplayName("BDD 16.4 - Excluir receita de outro usuário lança RegistroNaoEncontradoException (404)")
    void excluir_quandoReceitaDeOutroUsuario_deveLancarExcecao() {
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(70L, 1L)).thenReturn(Optional.empty());

        assertThrows(br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException.class,
            () -> receitaService.excluir(70L, 1L, "user1"));
        verify(receitaRepository, never()).save(any());
    }

    @Test
    @DisplayName("BDD 16.13 / RN09 - Excluir receita MANUAL faz exclusão lógica sem checar uso")
    void excluir_receitaManual_deveFazerExclusaoLogica() {
        Receita receita = criarReceita(9L, 1L, false, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(9L, 1L)).thenReturn(Optional.of(receita));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        receitaService.excluir(9L, 1L, "user1");

        assertTrue(receita.isExcluido());
        assertNotNull(receita.getDataExclusao());
        assertEquals("user1", receita.getExcluidoPor());
        verify(receitaRepository).save(receita);
    }

    @Test
    @DisplayName("BDD 16.14 / RN08 - Excluir receita do Open Finance lança RegraNegocioException e não altera nada")
    void excluir_receitaOpenFinance_deveLancarExcecaoENaoAlterar() {
        Receita receita = criarReceita(10L, 1L, false, false);
        receita.setOrigem(OrigemLancamento.OPEN_FINANCE);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(receita));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> receitaService.excluir(10L, 1L, "user1"));
        assertEquals("msg.receita.importada.nao-excluivel", ex.getMessage());
        assertFalse(receita.isExcluido());
        verify(receitaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN15 - Duplicar receita com lista vazia lança RegraNegocioException")
    void duplicar_comListaVazia_deveLancarExcecao() {
        assertThrows(RegraNegocioException.class, () -> receitaService.duplicar(List.of(), null, 1L, "user1"));
    }

    @Test
    @DisplayName("RN15 - Duplicar receitas com sucesso para mesma competência e como previstas")
    void duplicar_comSucesso_mesmaCompetencia() {
        Receita r1 = criarReceita(1L, 1L, true, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(1L, 1L)).thenReturn(Optional.of(r1));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Receita> duplicadas = receitaService.duplicar(List.of(1L), null, 1L, "user1");

        assertEquals(1, duplicadas.size());
        Receita copia = duplicadas.get(0);
        assertEquals(r1.getNome(), copia.getNome());
        assertEquals(r1.getValor(), copia.getValor());
        assertEquals(r1.getCompetencia(), copia.getCompetencia());
        assertFalse(copia.isRecebido());
        assertNull(copia.getDataRecebimento());
        assertEquals(OrigemLancamento.MANUAL, copia.getOrigem());
        verify(receitaRepository).save(any());
    }

    @Test
    @DisplayName("RN15 - Duplicar receitas com sucesso para competência alvo informada")
    void duplicar_comSucesso_novaCompetencia() {
        Receita r1 = criarReceita(1L, 1L, false, false);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(1L, 1L)).thenReturn(Optional.of(r1));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Receita> duplicadas = receitaService.duplicar(List.of(1L), "2026-10", 1L, "user1");

        assertEquals(1, duplicadas.size());
        Receita copia = duplicadas.get(0);
        assertEquals(YearMonth.of(2026, 10), copia.getCompetencia());
        assertFalse(copia.isRecebido());
    }
}
