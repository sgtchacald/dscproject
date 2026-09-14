package br.com.diegocordeiro.dscproject.service.cartao;

import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoFormDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoGridDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoCreditoServiceTest {

    @Mock
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ParametroGlobalRepository parametroGlobalRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private CartaoCreditoService service;

    @BeforeEach
    void setUp() {
        service = new CartaoCreditoService(cartaoCreditoRepository, contaRepository, usuarioRepository, parametroGlobalRepository, jdbcTemplate);
    }

    private Usuario criarUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setLogin("user" + id);
        return u;
    }

    @Test
    @DisplayName("BDD 16.0 / RN02 - Lista apenas os cartões do usuário autenticado, com dados da conta de débito")
    void listarParaGrid_deveRetornarCartoesDoUsuarioComContagemDeVinculos() {
        Conta conta = new Conta();
        conta.setId(7L);
        conta.setDescricao("Conta Corrente");

        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setDescricao("Nubank Ultravioleta");
        cartao.setBandeira("MASTERCARD");
        cartao.setFinalCartao("1234");
        cartao.setLimite(new BigDecimal("5000.00"));
        cartao.setDiaFechamento(3);
        cartao.setDiaVencimento(10);
        cartao.setConta(conta);
        cartao.setAtivo(true);
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.listarPorUsuario(1L)).thenReturn(List.of(cartao));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(1L))).thenReturn(0L);

        List<CartaoCreditoGridDTO> grid = service.listarParaGrid(1L);

        assertEquals(1, grid.size());
        CartaoCreditoGridDTO dto = grid.get(0);
        assertEquals("Nubank Ultravioleta", dto.getDescricao());
        assertEquals("MASTERCARD", dto.getBandeira());
        assertEquals("1234", dto.getFinalCartao());
        assertEquals(7L, dto.getContaId());
        assertEquals("Conta Corrente", dto.getContaDescricao());
        assertTrue(dto.isAtivo());
        assertFalse(dto.isExcluido());
        assertEquals(0L, dto.getQtdVinculos());
    }

    @Test
    @DisplayName("Cartão sem conta de débito expõe contaId/contaDescricao nulos")
    void listarParaGrid_semContaDebito_deveExporContaNula() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(2L);
        cartao.setDescricao("Cartão sem conta");
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.listarPorUsuario(1L)).thenReturn(List.of(cartao));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(2L))).thenReturn(0L);

        CartaoCreditoGridDTO dto = service.listarParaGrid(1L).get(0);
        assertNull(dto.getContaId());
        assertNull(dto.getContaDescricao());
    }

    @Test
    @DisplayName("RN08/C4 - Contabiliza vínculos de faturas e despesas para exibir na grid")
    void listarParaGrid_comVinculos_deveSomarFaturasEDespesas() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(3L);
        cartao.setDescricao("Nubank");
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.listarPorUsuario(1L)).thenReturn(List.of(cartao));
        when(jdbcTemplate.queryForObject(contains("FATURAS_CARTAO"), eq(Long.class), eq(3L))).thenReturn(1L);
        when(jdbcTemplate.queryForObject(contains("DESPESAS"), eq(Long.class), eq(3L))).thenReturn(2L);

        CartaoCreditoGridDTO dto = service.listarParaGrid(1L).get(0);
        assertEquals(3L, dto.getQtdVinculos());
    }

    @Test
    @DisplayName("BDD 16.3 / RN02 - Buscar cartão de outro usuário lança RegistroNaoEncontradoException (404)")
    void buscarParaEdicao_quandoCartaoDeOutroUsuario_deveLancarRegistroNaoEncontradoException() {
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () -> service.buscarParaEdicao(50L, 1L));
    }

    @Test
    @DisplayName("BDD 16.4 - Cadastrar cartão com dados válidos vincula ao usuário autenticado e fica ativo")
    void inserir_comDadosValidos_deveSalvarComoAtivoDoUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L)));
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> {
            CartaoCredito c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Nubank Ultravioleta");
        dto.setBandeira("MASTERCARD");
        dto.setFinalCartao("1234");
        dto.setDiaFechamento(3);
        dto.setDiaVencimento(10);

        CartaoCredito salvo = service.inserir(dto, 1L, "user1");

        assertNotNull(salvo);
        assertEquals("Nubank Ultravioleta", salvo.getDescricao());
        assertTrue(salvo.isAtivo());
        assertEquals(1L, salvo.getUsuario().getId());
        assertNull(salvo.getConta());
        verify(cartaoCreditoRepository).save(any(CartaoCredito.class));
    }

    @Test
    @DisplayName("BDD 16.8 - Cadastrar cartão sem conta de débito cria com CTA_ID nulo")
    void inserir_semContaDebito_deveCriarComContaNula() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L)));
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Cartão sem conta");
        dto.setContaId(null);

        CartaoCredito salvo = service.inserir(dto, 1L, "user1");
        assertNull(salvo.getConta());
    }

    @Test
    @DisplayName("Cadastrar cartão com conta de débito válida vincula a conta")
    void inserir_comContaDebitoValida_deveVincularConta() {
        Conta conta = new Conta();
        conta.setId(7L);
        conta.setAtivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L)));
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(7L, 1L)).thenReturn(Optional.of(conta));
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Nubank");
        dto.setContaId(7L);

        CartaoCredito salvo = service.inserir(dto, 1L, "user1");
        assertEquals(7L, salvo.getConta().getId());
    }

    @Test
    @DisplayName("BDD 16.13 / RN10 - Editar dia de vencimento de cartão com faturas existentes é permitido")
    void editar_diasComFaturaExistente_devePermitir() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(50L);
        cartao.setDescricao("Nubank");
        cartao.setDiaVencimento(10);
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(cartao));
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Nubank");
        dto.setDiaVencimento(15);
        dto.setAtivo(true);

        CartaoCredito editado = service.editar(50L, dto, 1L, "user1");
        assertEquals(15, editado.getDiaVencimento());
        verify(cartaoCreditoRepository).save(any());
    }

    @Test
    @DisplayName("RN09 - Editar com ativo=false desativa o cartão sem afetar outros campos")
    void editar_comAtivoFalse_deveDesativarCartao() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(50L);
        cartao.setDescricao("Nubank");
        cartao.setAtivo(true);
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(cartao));
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Nubank");
        dto.setAtivo(false);

        CartaoCredito editado = service.editar(50L, dto, 1L, "user1");
        assertFalse(editado.isAtivo());
    }

    @Test
    @DisplayName("Editar cartão de outro usuário lança RegistroNaoEncontradoException")
    void editar_quandoCartaoDeOutroUsuario_deveLancarExcecao() {
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(99L, 1L)).thenReturn(Optional.empty());

        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Qualquer");

        assertThrows(RegistroNaoEncontradoException.class, () -> service.editar(99L, dto, 1L, "user1"));
        verify(cartaoCreditoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BDD 16.9 / RN08 - Não excluir cartão com fatura vinculada, mesmo com o parâmetro desligado")
    void excluir_comFaturaVinculada_deveLancarExcecaoSempre() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(50L);
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(cartao));
        when(jdbcTemplate.queryForObject(contains("FATURAS_CARTAO"), eq(Long.class), eq(50L))).thenReturn(1L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.excluir(50L, 1L, "user1"));
        assertEquals("msg.cartao.em-uso.bloqueada", ex.getMessage());
        verify(cartaoCreditoRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN08 - Não excluir cartão com despesa vinculada quando o parâmetro bloqueia")
    void excluir_comDespesaEParametroBloqueia_deveLancarExcecao() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(50L);
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(cartao));
        when(jdbcTemplate.queryForObject(contains("FATURAS_CARTAO"), eq(Long.class), eq(50L))).thenReturn(0L);
        when(jdbcTemplate.queryForObject(contains("DESPESAS"), eq(Long.class), eq(50L))).thenReturn(2L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.excluir(50L, 1L, "user1"));
        assertEquals("msg.cartao.em-uso.bloqueada", ex.getMessage());
        verify(cartaoCreditoRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN08 - Excluir cartão com despesa vinculada quando o parâmetro não bloqueia desvincula a despesa")
    void excluir_comDespesaEParametroNaoBloqueia_deveDesvincularEExcluir() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(50L);
        cartao.setUsuario(criarUsuario(1L));

        br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal parametro = new br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal();
        parametro.setValor("false");

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(cartao));
        when(jdbcTemplate.queryForObject(contains("FATURAS_CARTAO"), eq(Long.class), eq(50L))).thenReturn(0L);
        when(jdbcTemplate.queryForObject(contains("DESPESAS"), eq(Long.class), eq(50L))).thenReturn(2L);
        when(parametroGlobalRepository.findByCodigo("CARTAO_EXCLUSAO_BLOQUEIA_EM_USO")).thenReturn(Optional.of(parametro));
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.excluir(50L, 1L, "user1");

        assertTrue(cartao.isExcluido());
        verify(jdbcTemplate).update(contains("UPDATE DESPESAS"), eq(50L));
        verify(cartaoCreditoRepository).save(cartao);
    }

    @Test
    @DisplayName("BDD 16.11 / RN11 - Excluir cartão sem vínculo faz a exclusão lógica")
    void excluir_semVinculo_deveExcluirLogicamente() {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(50L);
        cartao.setUsuario(criarUsuario(1L));

        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(cartao));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(50L))).thenReturn(0L);
        when(cartaoCreditoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.excluir(50L, 1L, "user1");

        assertTrue(cartao.isExcluido());
        assertNotNull(cartao.getDataExclusao());
        assertEquals("user1", cartao.getExcluidoPor());
        verify(cartaoCreditoRepository).save(cartao);
    }

    @Test
    @DisplayName("Excluir cartão de outro usuário lança RegistroNaoEncontradoException")
    void excluir_quandoCartaoDeOutroUsuario_deveLancarExcecao() {
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () -> service.excluir(99L, 1L, "user1"));
        verify(cartaoCreditoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BDD 16.12 - Combobox de despesa retorna apenas os cartões ativos do usuário")
    void listarOpcoesCombobox_deveRetornarApenasAtivos() {
        CartaoCredito nubank = new CartaoCredito();
        nubank.setId(1L);
        nubank.setDescricao("Nubank");
        nubank.setBandeira("MASTERCARD");
        nubank.setFinalCartao("1234");
        nubank.setDiaFechamento(3);
        nubank.setDiaVencimento(10);

        when(cartaoCreditoRepository.listarAtivasPorUsuario(1L)).thenReturn(List.of(nubank));

        List<CartaoCreditoOpcaoDTO> opcoes = service.listarOpcoesCombobox(1L);
        assertEquals(1, opcoes.size());
        assertEquals("Nubank", opcoes.get(0).getDescricao());
        assertEquals("1234", opcoes.get(0).getFinalCartao());
    }

    @Test
    @DisplayName("Combobox de despesa usa cache por usuário quando o parâmetro está ligado")
    void listarOpcoesCombobox_comCacheHabilitado_naoConsultaRepositorioDeNovo() {
        br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal parametroCache = new br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal();
        parametroCache.setValor("true");

        when(parametroGlobalRepository.findByCodigo("CARTAO_COMBOBOX_CACHE")).thenReturn(Optional.of(parametroCache));
        when(cartaoCreditoRepository.listarAtivasPorUsuario(1L)).thenReturn(List.of());

        service.listarOpcoesCombobox(1L);
        service.listarOpcoesCombobox(1L);

        verify(cartaoCreditoRepository, org.mockito.Mockito.times(1)).listarAtivasPorUsuario(1L);
    }
}
