package br.com.diegocordeiro.dscproject.service.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstituicaoFinanceiraServiceTest {

    @Mock
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @Mock
    private OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository;

    @Mock
    private ParametroGlobalRepository parametroGlobalRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private InstituicaoFinanceiraService instituicaoFinanceiraService;

    @BeforeEach
    void setUp() {
        instituicaoFinanceiraService = new InstituicaoFinanceiraService(
                instituicaoFinanceiraRepository,
                opfiInstituicaoProvedorRepository,
                parametroGlobalRepository,
                jdbcTemplate
        );
    }

    @Test
    @DisplayName("RN02 - Deve impedir cadastro de instituição com nome duplicado")
    void inserir_quandoNomeDuplicado_deveLancarExcecao() {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Teste", null)).thenReturn(1L);

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setNome("Banco Teste");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoFinanceiraService.inserir(dto));
        assertEquals("msg.instituicao.nome.duplicado", ex.getMessage());
        assertEquals("nome", ex.getCampo());
        verify(instituicaoFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN03 - Deve impedir cadastro de instituição com código duplicado")
    void inserir_quandoCodigoDuplicado_deveLancarExcecao() {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Novo", null)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.contarPorCodigo("001", null)).thenReturn(1L);

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setNome("Banco Novo");
        dto.setCodigo("001");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoFinanceiraService.inserir(dto));
        assertEquals("msg.instituicao.codigo.duplicado", ex.getMessage());
        assertEquals("codigo", ex.getCampo());
        verify(instituicaoFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN02 / RN03 - Deve cadastrar instituição comum com sucesso")
    void inserir_comDadosValidos_deveSalvarComSucesso() {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Teste", null)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.contarPorCodigo("999", null)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.save(any())).thenAnswer(inv -> {
            InstituicaoFinanceira inst = inv.getArgument(0);
            inst.setId(10L);
            return inst;
        });

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setNome("Banco Teste");
        dto.setCodigo("999");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        InstituicaoFinanceira salva = instituicaoFinanceiraService.inserir(dto);
        assertNotNull(salva);
        assertEquals("Banco Teste", salva.getNome());
        assertEquals("999", salva.getCodigo());
        assertEquals(TipoInstituicaoFinanceira.BANCO, salva.getTipo());
        assertFalse(salva.isSistema());
        assertTrue(salva.isAtivo());
        verify(instituicaoFinanceiraRepository).save(any(InstituicaoFinanceira.class));
    }

    @Test
    @DisplayName("RN05 - Instituição de sistema: impede alteração do nome")
    void editar_instituicaoSistema_quandoAlteraNome_deveLancarExcecao() {
        InstituicaoFinanceira sistema = new InstituicaoFinanceira();
        sistema.setId(1L);
        sistema.setNome("Banco do Brasil");
        sistema.setCodigo("001");
        sistema.setTipo(TipoInstituicaoFinanceira.BANCO);
        sistema.setSistema(true);

        when(instituicaoFinanceiraRepository.findById(1L)).thenReturn(Optional.of(sistema));

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setId(1L);
        dto.setNome("Novo Banco");
        dto.setCodigo("001");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoFinanceiraService.editar(1L, dto));
        assertEquals("msg.instituicao.sistema.imutavel", ex.getMessage());
        verify(instituicaoFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN05 - Instituição de sistema: impede alteração do código")
    void editar_instituicaoSistema_quandoAlteraCodigo_deveLancarExcecao() {
        InstituicaoFinanceira sistema = new InstituicaoFinanceira();
        sistema.setId(1L);
        sistema.setNome("Banco do Brasil");
        sistema.setCodigo("001");
        sistema.setTipo(TipoInstituicaoFinanceira.BANCO);
        sistema.setSistema(true);

        when(instituicaoFinanceiraRepository.findById(1L)).thenReturn(Optional.of(sistema));

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setId(1L);
        dto.setNome("Banco do Brasil");
        dto.setCodigo("999");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoFinanceiraService.editar(1L, dto));
        assertEquals("msg.instituicao.sistema.imutavel", ex.getMessage());
        verify(instituicaoFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN05 - Instituição de sistema: permite alterar tipo e situação")
    void editar_instituicaoSistema_quandoAlteraTipoESituacao_deveSalvar() {
        InstituicaoFinanceira sistema = new InstituicaoFinanceira();
        sistema.setId(1L);
        sistema.setNome("Banco do Brasil");
        sistema.setCodigo("001");
        sistema.setTipo(TipoInstituicaoFinanceira.BANCO);
        sistema.setSistema(true);
        sistema.setAtivo(true);

        when(instituicaoFinanceiraRepository.findById(1L)).thenReturn(Optional.of(sistema));
        when(instituicaoFinanceiraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setId(1L);
        dto.setNome("Banco do Brasil");
        dto.setCodigo("001");
        dto.setTipo(TipoInstituicaoFinanceira.CORRETORA);
        dto.setAtivo(false);

        InstituicaoFinanceira atualizada = instituicaoFinanceiraService.editar(1L, dto);
        assertEquals(TipoInstituicaoFinanceira.CORRETORA, atualizada.getTipo());
        assertFalse(atualizada.isAtivo());
        verify(instituicaoFinanceiraRepository).save(any());
    }

    @Test
    @DisplayName("RN06 - Não pode excluir instituição de sistema")
    void excluir_quandoInstituicaoSistema_deveLancarExcecao() {
        InstituicaoFinanceira sistema = new InstituicaoFinanceira();
        sistema.setId(1L);
        sistema.setNome("Banco do Brasil");
        sistema.setSistema(true);

        when(instituicaoFinanceiraRepository.findById(1L)).thenReturn(Optional.of(sistema));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoFinanceiraService.excluir(1L, "ADMIN"));
        assertEquals("msg.instituicao.sistema.imutavel", ex.getMessage());
        verify(instituicaoFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN07 - Não excluir instituição em uso quando parâmetro bloqueia")
    void excluir_quandoEmUsoEParametroBloqueia_deveLancarExcecao() {
        InstituicaoFinanceira comum = new InstituicaoFinanceira();
        comum.setId(2L);
        comum.setNome("Banco Comum");
        comum.setSistema(false);

        when(instituicaoFinanceiraRepository.findById(2L)).thenReturn(Optional.of(comum));
        when(jdbcTemplate.queryForObject(contains("CONTAS"), eq(Long.class), eq(2L))).thenReturn(3L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoFinanceiraService.excluir(2L, "ADMIN"));
        assertEquals("msg.instituicao.em-uso.bloqueada", ex.getMessage());
        verify(instituicaoFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN07 / RN12 - Excluir logicamente instituição sem uso e realizar exclusão lógica em cascata de vínculos")
    void excluir_quandoSemUso_deveRealizarSoftDeleteECascata() {
        InstituicaoFinanceira comum = new InstituicaoFinanceira();
        comum.setId(3L);
        comum.setNome("Banco Sem Uso");
        comum.setSistema(false);

        OpfiInstituicaoProvedor vinculo = new OpfiInstituicaoProvedor();
        vinculo.setId(10L);

        when(instituicaoFinanceiraRepository.findById(3L)).thenReturn(Optional.of(comum));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(3L))).thenReturn(0L);
        when(opfiInstituicaoProvedorRepository.findByInstituicaoIdAndDataExclusaoIsNull(3L)).thenReturn(List.of(vinculo));

        instituicaoFinanceiraService.excluir(3L, "ADMIN");

        assertTrue(comum.isExcluido());
        assertNotNull(comum.getDataExclusao());
        assertEquals("ADMIN", comum.getExcluidoPor());
        verify(instituicaoFinanceiraRepository).save(comum);

        assertTrue(vinculo.isExcluido());
        assertEquals("ADMIN", vinculo.getExcluidoPor());
        assertNotNull(vinculo.getDataExclusao());
        verify(opfiInstituicaoProvedorRepository).save(vinculo);
    }

    @Test
    @DisplayName("Desativar instituição altera flag ativo para false")
    void desativar_deveAlterarAtivoParaFalse() {
        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);
        inst.setAtivo(true);

        when(instituicaoFinanceiraRepository.findById(5L)).thenReturn(Optional.of(inst));

        instituicaoFinanceiraService.desativar(5L);

        assertFalse(inst.isAtivo());
        verify(instituicaoFinanceiraRepository).save(inst);
    }

    @Test
    @DisplayName("RN10 - Combobox retorna apenas instituições ativas e não excluídas")
    void listarOpcoesCombobox_deveRetornarApenasAtivas() {
        InstituicaoFinanceira inst1 = new InstituicaoFinanceira();
        inst1.setId(1L);
        inst1.setNome("Banco Inter");
        inst1.setCodigo("077");
        inst1.setTipo(TipoInstituicaoFinanceira.BANCO);
        inst1.setAtivo(true);

        when(instituicaoFinanceiraRepository.listarAtivasPorTipo(TipoInstituicaoFinanceira.BANCO)).thenReturn(List.of(inst1));

        List<InstituicaoFinanceiraOpcaoDTO> opcoes = instituicaoFinanceiraService.listarOpcoesCombobox(TipoInstituicaoFinanceira.BANCO);
        assertEquals(1, opcoes.size());
        assertEquals("Banco Inter", opcoes.get(0).getNome());
        assertEquals("077", opcoes.get(0).getCodigo());
    }
}
