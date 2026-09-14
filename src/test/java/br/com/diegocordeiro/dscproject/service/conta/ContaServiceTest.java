package br.com.diegocordeiro.dscproject.service.conta;

import br.com.diegocordeiro.dscproject.dto.conta.ContaAjusteSaldoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaFormDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ParametroGlobalRepository parametroGlobalRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ContaService contaService;

    @BeforeEach
    void setUp() {
        contaService = new ContaService(
                contaRepository,
                instituicaoFinanceiraRepository,
                usuarioRepository,
                parametroGlobalRepository,
                jdbcTemplate
        );
    }

    private Usuario criarUsuario(Long id, String login) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setLogin(login);
        u.setNome("Usuário " + login);
        return u;
    }

    @Test
    @DisplayName("RN03 - Deve impedir cadastro de conta com descrição duplicada para o mesmo usuário")
    void inserir_quandoDescricaoDuplicadaParaUsuario_deveLancarExcecao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L, "user1")));
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta Principal", null)).thenReturn(1L);

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta Principal");
        dto.setInstituicaoId(10L);
        dto.setTipo(TipoConta.CORRENTE);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> contaService.inserir(dto, 1L, "user1"));
        assertEquals("msg.conta.descricao.duplicada", ex.getMessage());
        assertEquals("descricao", ex.getCampo());
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN06 - Deve impedir cadastro com instituição inativa")
    void inserir_quandoInstituicaoInativa_deveLancarExcecao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L, "user1")));
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta Nova", null)).thenReturn(0L);

        InstituicaoFinanceira instInativa = new InstituicaoFinanceira();
        instInativa.setId(10L);
        instInativa.setAtivo(false);

        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(instInativa));

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta Nova");
        dto.setInstituicaoId(10L);
        dto.setTipo(TipoConta.CORRENTE);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> contaService.inserir(dto, 1L, "user1"));
        assertEquals("msg.conta.instituicao.invalida", ex.getMessage());
        assertEquals("instituicaoId", ex.getCampo());
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN02 / RN03 / RN05 / RN10 - Deve cadastrar conta com sucesso")
    void inserir_comDadosValidos_deveSalvarComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L, "user1")));
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta Salário", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);
        inst.setNome("Banco do Brasil");
        inst.setAtivo(true);

        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(5L)).thenReturn(Optional.of(inst));
        when(contaRepository.save(any())).thenAnswer(inv -> {
            Conta c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta Salário");
        dto.setInstituicaoId(5L);
        dto.setTipo(TipoConta.CORRENTE);
        dto.setSaldoInicial(new BigDecimal("1500.00"));
        dto.setConsideraSaldo(true);

        Conta salva = contaService.inserir(dto, 1L, "user1");
        assertNotNull(salva);
        assertEquals("Conta Salário", salva.getDescricao());
        assertEquals(new BigDecimal("1500.00"), salva.getSaldo());
        assertEquals("BRL", salva.getMoeda());
        assertTrue(salva.isAtivo());
        assertFalse(salva.isExcluido());
        assertEquals(1L, salva.getUsuario().getId());
        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    @DisplayName("RN05 - Moeda informada deve ser normalizada para maiúsculas")
    void inserir_comMoedaEmMinusculas_deveSalvarEmMaiusculas() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L, "user1")));
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta em Dólar", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);
        inst.setNome("XP Investimentos");
        inst.setAtivo(true);

        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(5L)).thenReturn(Optional.of(inst));
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta em Dólar");
        dto.setInstituicaoId(5L);
        dto.setTipo(TipoConta.INVESTIMENTO);
        dto.setMoeda("usd");

        Conta salva = contaService.inserir(dto, 1L, "user1");
        assertEquals("USD", salva.getMoeda());
    }

    @Test
    @DisplayName("RN02 - Buscar conta de outro usuário deve lançar RegistroNaoEncontradoException (404)")
    void buscarParaEdicao_quandoContaDeOutroUsuario_deveLancarRegistroNaoEncontradoException() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () -> contaService.buscarParaEdicao(99L, 1L));
    }

    @Test
    @DisplayName("RN06 - Editar conta tentando alterar instituição deve lançar RegraNegocioException")
    void editar_quandoAlteraInstituicao_deveLancarExcecao() {
        InstituicaoFinanceira instOriginal = new InstituicaoFinanceira();
        instOriginal.setId(10L);

        Conta conta = new Conta();
        conta.setId(50L);
        conta.setDescricao("Minha Conta");
        conta.setInstituicao(instOriginal);
        conta.setUsuario(criarUsuario(1L, "user1"));

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(conta));

        ContaFormDTO dto = new ContaFormDTO();
        dto.setId(50L);
        dto.setDescricao("Minha Conta Atualizada");
        dto.setInstituicaoId(20L); // mudou instituicao
        dto.setTipo(TipoConta.CORRENTE);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> contaService.editar(50L, dto, 1L, "user1"));
        assertEquals("msg.conta.instituicao.imutavel", ex.getMessage());
        assertEquals("instituicaoId", ex.getCampo());
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN02 / RN10 - Editar conta atualiza atributos sem alterar saldo atual")
    void editar_comDadosValidos_deveAtualizarCamposPermitidos() {
        InstituicaoFinanceira instOriginal = new InstituicaoFinanceira();
        instOriginal.setId(10L);

        Conta conta = new Conta();
        conta.setId(50L);
        conta.setDescricao("Minha Conta");
        conta.setInstituicao(instOriginal);
        conta.setSaldo(new BigDecimal("2000.00"));
        conta.setUsuario(criarUsuario(1L, "user1"));

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(conta));
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Minha Conta Atualizada", 50L)).thenReturn(0L);
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaFormDTO dto = new ContaFormDTO();
        dto.setId(50L);
        dto.setDescricao("Minha Conta Atualizada");
        dto.setInstituicaoId(10L);
        dto.setTipo(TipoConta.POUPANCA);
        dto.setAgencia("1234");
        dto.setNumero("5678-9");
        dto.setConsideraSaldo(false);
        dto.setAtivo(false);
        dto.setSaldoInicial(new BigDecimal("99999.00")); // deve ser ignorado na edição

        Conta editada = contaService.editar(50L, dto, 1L, "user1");
        assertEquals("Minha Conta Atualizada", editada.getDescricao());
        assertEquals(TipoConta.POUPANCA, editada.getTipo());
        assertEquals("1234", editada.getAgencia());
        assertEquals("5678-9", editada.getNumero());
        assertFalse(editada.isConsideraSaldo());
        assertFalse(editada.isAtivo());
        // Saldo não foi afetado pelo saldoInicial informado no DTO
        assertEquals(new BigDecimal("2000.00"), editada.getSaldo());
        verify(contaRepository).save(any());
    }

    @Test
    @DisplayName("RN10 - Ajustar saldo atualiza valor diretamente")
    void ajustarSaldo_comNovoSaldo_deveAtualizarSaldo() {
        Conta conta = new Conta();
        conta.setId(50L);
        conta.setSaldo(new BigDecimal("100.00"));
        conta.setUsuario(criarUsuario(1L, "user1"));

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(conta));

        ContaAjusteSaldoDTO dto = new ContaAjusteSaldoDTO();
        dto.setNovoSaldo(new BigDecimal("250.75"));
        dto.setObservacao("Ajuste de conciliação bancária");

        contaService.ajustarSaldo(50L, dto, 1L, "user1");

        assertEquals(new BigDecimal("250.75"), conta.getSaldo());
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("RN08 - Desativar conta altera flag ativo para false")
    void desativar_deveAlterarAtivoParaFalse() {
        Conta conta = new Conta();
        conta.setId(50L);
        conta.setAtivo(true);
        conta.setUsuario(criarUsuario(1L, "user1"));

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(conta));

        contaService.desativar(50L, 1L);

        assertFalse(conta.isAtivo());
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("RN07 - Não excluir conta em uso quando parâmetro bloqueia")
    void excluir_quandoEmUsoEParametroBloqueia_deveLancarExcecao() {
        Conta conta = new Conta();
        conta.setId(50L);
        conta.setUsuario(criarUsuario(1L, "user1"));

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(conta));
        when(jdbcTemplate.queryForObject(contains("RECEITAS"), eq(Long.class), eq(50L))).thenReturn(2L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> contaService.excluir(50L, 1L, "user1"));
        assertEquals("msg.conta.em-uso.bloqueada", ex.getMessage());
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN07 / RN11 - Excluir logicamente conta sem uso")
    void excluir_quandoSemUso_deveRealizarSoftDelete() {
        Conta conta = new Conta();
        conta.setId(50L);
        conta.setUsuario(criarUsuario(1L, "user1"));

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(50L, 1L)).thenReturn(Optional.of(conta));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(50L))).thenReturn(0L);

        contaService.excluir(50L, 1L, "user1");

        assertTrue(conta.isExcluido());
        assertNotNull(conta.getDataExclusao());
        assertEquals("user1", conta.getExcluidoPor());
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("Combobox retorna apenas contas ativas e formata descrição com instituição")
    void listarOpcoesCombobox_deveRetornarApenasAtivas() {
        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setNome("Nubank");

        Conta c1 = new Conta();
        c1.setId(1L);
        c1.setDescricao("Conta Pessoal");
        c1.setInstituicao(inst);
        c1.setAtivo(true);

        when(contaRepository.listarAtivasPorUsuario(1L)).thenReturn(List.of(c1));

        List<ContaOpcaoDTO> opcoes = contaService.listarOpcoesCombobox(1L);
        assertEquals(1, opcoes.size());
        assertEquals(1L, opcoes.get(0).getId());
        assertEquals("Conta Pessoal", opcoes.get(0).getDescricao());
        assertEquals("Nubank", opcoes.get(0).getInstituicaoNome());
    }
}
