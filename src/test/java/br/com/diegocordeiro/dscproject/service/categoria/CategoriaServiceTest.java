package br.com.diegocordeiro.dscproject.service.categoria;

import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFormDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosCategoriaCatalogo;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ParametroGlobalRepository parametroGlobalRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, parametroGlobalRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("RN02 / RT08 - Normalização de código: maiúsculas, sem acentos, espaços viram underscore")
    void normalizarCodigo_deveRemoverAcentosEConverterParaMaiusculo() {
        assertEquals("CARTAO_DE_CREDITO", CategoriaService.normalizarCodigo("Cartão de Crédito"));
        assertEquals("ALIMENTACAO", CategoriaService.normalizarCodigo("alimentação"));
        assertEquals("13_SALARIO", CategoriaService.normalizarCodigo("13º Salário"));
        assertEquals("PETS", CategoriaService.normalizarCodigo("  pets  "));
    }

    @Test
    @DisplayName("RN02 - Deve impedir cadastro de categoria com código duplicado")
    void inserir_quandoCodigoDuplicado_deveLancarExcecao() {
        when(categoriaRepository.contarPorCodigo("ALIMENTACAO", null)).thenReturn(1L);

        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setCodigo("alimentação");
        dto.setNome("Alimentação");
        dto.setAplicaA(AplicaA.DESPESA);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> categoriaService.inserir(dto));
        assertEquals("categoria.codigo.duplicado", ex.getMessage());
        assertEquals("codigo", ex.getCampo());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN02 / RN03 - Deve cadastrar categoria comum ativa com sucesso")
    void inserir_comDadosValidos_deveSalvarComSucesso() {
        when(categoriaRepository.contarPorCodigo("PETS", null)).thenReturn(0L);
        when(categoriaRepository.save(any())).thenAnswer(invocation -> {
            Categoria c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setCodigo("pets");
        dto.setNome("Pets e Animais");
        dto.setAplicaA(AplicaA.DESPESA);
        dto.setCor("#ff0000");

        Categoria criada = categoriaService.inserir(dto);
        assertNotNull(criada);
        assertEquals("PETS", criada.getCodigo());
        assertEquals("Pets e Animais", criada.getNome());
        assertFalse(criada.isSistema());
        assertTrue(criada.isAtivo());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("RN04 - Categoria de sistema: deve impedir alteração do código")
    void editar_categoriaSistema_quandoAlteraCodigo_deveLancarExcecao() {
        Categoria sistema = new Categoria();
        sistema.setId(1L);
        sistema.setCodigo("SALARIO");
        sistema.setNome("Salário");
        sistema.setAplicaA(AplicaA.RECEITA);
        sistema.setSistema(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(sistema));

        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setId(1L);
        dto.setCodigo("OUTRO_CODIGO");
        dto.setNome("Salário Alterado");
        dto.setAplicaA(AplicaA.RECEITA);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> categoriaService.editar(1L, dto));
        assertEquals("categoria.sistema.codigo-imutavel", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN04 - Categoria de sistema: permite alterar nome, cor e situação mantendo código")
    void editar_categoriaSistema_quandoAlteraNomeECor_deveSalvar() {
        Categoria sistema = new Categoria();
        sistema.setId(1L);
        sistema.setCodigo("LAZER");
        sistema.setNome("Lazer");
        sistema.setAplicaA(AplicaA.DESPESA);
        sistema.setSistema(true);
        sistema.setAtivo(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(sistema));
        when(categoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setId(1L);
        dto.setCodigo("LAZER");
        dto.setNome("Lazer e Hobbies");
        dto.setAplicaA(AplicaA.DESPESA);
        dto.setCor("#00ff00");
        dto.setAtivo(false);

        Categoria alterada = categoriaService.editar(1L, dto);
        assertEquals("LAZER", alterada.getCodigo());
        assertEquals("Lazer e Hobbies", alterada.getNome());
        assertEquals("#00ff00", alterada.getCor());
        assertFalse(alterada.isAtivo());
        verify(categoriaRepository).save(any());
    }

    @Test
    @DisplayName("RN05 - Não pode excluir categoria de sistema")
    void excluir_quandoCategoriaSistema_deveLancarExcecao() {
        Categoria sistema = new Categoria();
        sistema.setId(1L);
        sistema.setCodigo("MORADIA");
        sistema.setSistema(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(sistema));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> categoriaService.excluir(1L, "ADMIN"));
        assertEquals("categoria.sistema.nao-excluivel", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN06 / C5 - Não excluir categoria em uso quando parâmetro bloqueia exclusão")
    void excluir_quandoEmUsoEParametroBloqueia_deveLancarExcecao() {
        Categoria comum = new Categoria();
        comum.setId(2L);
        comum.setCodigo("TESTE");
        comum.setSistema(false);

        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(comum));
        when(jdbcTemplate.queryForObject(contains("RECEITAS"), eq(Long.class), eq(2L))).thenReturn(2L);

        ParametroGlobal param = new ParametroGlobal();
        param.setCodigo(ParametrosCategoriaCatalogo.CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO.getCodigo());
        param.setValor("true");
        when(parametroGlobalRepository.findByCodigo(param.getCodigo())).thenReturn(Optional.of(param));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> categoriaService.excluir(2L, "ADMIN"));
        assertEquals("categoria.em-uso.bloqueada", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN06 / C5 - Excluir logicamente categoria comum sem uso")
    void excluir_quandoComumSemUso_deveRealizarSoftDelete() {
        Categoria comum = new Categoria();
        comum.setId(3L);
        comum.setCodigo("CATEGORIA_LIVRE");
        comum.setNome("Livre");
        comum.setSistema(false);

        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(comum));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(3L))).thenReturn(0L);

        categoriaService.excluir(3L, "ADMIN");

        assertTrue(comum.isExcluido());
        assertNotNull(comum.getDataExclusao());
        assertEquals("ADMIN", comum.getExcluidoPor());
        verify(categoriaRepository).save(comum);
    }

    @Test
    @DisplayName("RN09 - Desativar categoria altera flag ativo para false")
    void desativar_deveAlterarAtivoParaFalse() {
        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setAtivo(true);

        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));

        categoriaService.desativar(5L);

        assertFalse(categoria.isAtivo());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("RN03 / EDP05 - Combobox retorna apenas ativas filtradas por aplicaA")
    void listarOpcoesCombobox_deveRetornarApenasCategoriasAtivas() {
        Categoria c1 = new Categoria();
        c1.setId(1L);
        c1.setCodigo("ALUGUEL");
        c1.setNome("Aluguel");
        c1.setAplicaA(AplicaA.DESPESA);
        c1.setAtivo(true);

        when(categoriaRepository.listarAtivasPorAplicaA(AplicaA.DESPESA)).thenReturn(List.of(c1));

        List<CategoriaOpcaoDTO> opcoes = categoriaService.listarOpcoesCombobox(AplicaA.DESPESA);
        assertEquals(1, opcoes.size());
        assertEquals("ALUGUEL", opcoes.get(0).getCodigo());
    }
}
