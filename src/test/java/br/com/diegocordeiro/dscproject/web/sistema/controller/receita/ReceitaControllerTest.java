package br.com.diegocordeiro.dscproject.web.sistema.controller.receita;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaGridDTO;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.receita.Receita;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.receita.ReceitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceitaController.class)
@Import(SecurityConfig.class)
class ReceitaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private ReceitaService receitaService;

    @MockitoBean
    private ReceitaRepository receitaRepository;

    @MockitoBean
    private ContaRepository contaRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setLogin("user_teste");
        usuarioMock.setNome("Usuário Teste");

        when(usuarioRepository.findByLogin("user_teste")).thenReturn(Optional.of(usuarioMock));
    }

    @Test
    @DisplayName("BDD 16.0 - Listar view com autoridade PERM_RECEITAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_LISTAR")
    void listar_comPermissao_deveRetornar200EViewListar() throws Exception {
        mockMvc.perform(get("/receitas/listar"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/receita/listar"));
    }

    @Test
    @DisplayName("BDD 16.1 - Bloquear acesso de usuário sem PERM_RECEITAS_LISTAR à tela e à listagem")
    @WithMockUser(username = "user_teste", authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/receitas/listar"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/receitas/listar-dados"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("BDD 16.0 / 16.2 / 16.18 - Listar dados JSON da grid restrito ao usuário autenticado")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_LISTAR")
    void listarDados_comPermissao_deveRetornarJsonGridDoUsuario() throws Exception {
        when(receitaService.listarParaGrid(1L)).thenReturn(List.of(
                ReceitaGridDTO.builder()
                        .id(10L)
                        .competencia(YearMonth.of(2026, 9))
                        .nome("Salário")
                        .valor(new BigDecimal("5000.00"))
                        .dataLancamento(LocalDate.of(2026, 9, 5))
                        .recebido(false)
                        .origem(OrigemLancamento.MANUAL)
                        .contaId(1L)
                        .contaDescricao("Nubank Conta Corrente")
                        .excluido(false)
                        .build()
        ));

        mockMvc.perform(get("/receitas/listar-dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nome").value("Salário"))
                .andExpect(jsonPath("$[0].competencia").value("2026-09"))
                .andExpect(jsonPath("$[0].valor").value(5000.00))
                .andExpect(jsonPath("$[0].recebido").value(false))
                .andExpect(jsonPath("$[0].origem").value("MANUAL"));

        verify(receitaService).listarParaGrid(1L);
    }

    private Conta contaAtiva(Long id) {
        Conta c = new Conta();
        c.setId(id);
        c.setAtivo(true);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        c.setUsuario(usuario);
        return c;
    }

    @Test
    @DisplayName("BDD 16.5 - Cadastrar receita prevista com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void inserir_comDadosValidos_deveRetornarOk() throws Exception {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L)));
        when(receitaService.inserir(any(), eq(1L), eq("user_teste"))).thenReturn(new Receita());

        mockMvc.perform(post("/receitas/inserir")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "5000.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09")
                        .param("contaId", "10")
                        .param("recebido", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(receitaService).inserir(any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("BDD 16.6 / MSG03 - Cadastrar com valor zero retorna 422 como erro de campo")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void inserir_comValorZero_deveRetornar422() throws Exception {
        mockMvc.perform(post("/receitas/inserir")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "0.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09")
                        .param("contaId", "10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.valor").exists());

        verify(receitaService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("C4 / MSG14 - Cadastrar com categoria inválida retorna 422 como erro de campo")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void inserir_comCategoriaInvalida_deveRetornar422() throws Exception {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L)));
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/receitas/inserir")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "5000.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09")
                        .param("contaId", "10")
                        .param("categoriaId", "99"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.categoriaId").exists());

        verify(receitaService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("BDD 16.11 / MSG02 - Cadastrar sem conta retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void inserir_semConta_deveRetornar422() throws Exception {
        mockMvc.perform(post("/receitas/inserir")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "5000.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.contaId").exists());

        verify(receitaService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("BDD 16.12 - Cadastrar com conta de outro usuário retorna 422 (conta inválida)")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void inserir_comContaDeOutroUsuario_deveRetornar422() throws Exception {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/receitas/inserir")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "5000.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09")
                        .param("contaId", "10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.contaId").exists());

        verify(receitaService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("MSG11 - Cadastrar com competência fora do formato AAAA-MM retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void inserir_comCompetenciaForaDoFormato_deveRetornar422() throws Exception {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L)));

        mockMvc.perform(post("/receitas/inserir")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "5000.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "09/2026")
                        .param("contaId", "10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errosCampos.competencia").exists());

        verify(receitaService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("BDD 16.3 - Buscar receita de outro usuário retorna 404")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EDITAR")
    void buscar_quandoReceitaDeOutroUsuario_deveRetornar404() throws Exception {
        when(receitaService.buscarParaEdicao(70L, 1L))
                .thenThrow(new br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException("msg.receita.nao-encontrada"));

        mockMvc.perform(get("/receitas/buscar/70"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("EDP03 - Buscar receita do próprio usuário retorna os dados para edição")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EDITAR")
    void buscar_quandoReceitaDoUsuario_deveRetornarDadosDeEdicao() throws Exception {
        when(receitaService.buscarParaEdicao(10L, 1L)).thenReturn(
                br.com.diegocordeiro.dscproject.dto.receita.ReceitaEdicaoDTO.builder()
                        .id(10L)
                        .nome("Salário")
                        .competencia(YearMonth.of(2026, 9))
                        .valor(new BigDecimal("5000.00"))
                        .dataLancamento(LocalDate.of(2026, 9, 5))
                        .origem(OrigemLancamento.MANUAL)
                        .contaId(1L)
                        .build()
        );

        mockMvc.perform(get("/receitas/buscar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Salário"))
                .andExpect(jsonPath("$.origem").value("MANUAL"));
    }

    @Test
    @DisplayName("BDD 16.4 - Editar receita de outro usuário retorna 404")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EDITAR")
    void editar_quandoReceitaDeOutroUsuario_deveRetornar404() throws Exception {
        when(receitaService.buscarPorIdEUsuario(70L, 1L))
                .thenThrow(new br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException("msg.receita.nao-encontrada"));

        mockMvc.perform(put("/receitas/editar/70")
                        .with(csrf())
                        .param("nome", "Salário")
                        .param("valor", "5000.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09")
                        .param("contaId", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BDD 16.15 - Editar receita com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EDITAR")
    void editar_comDadosValidos_deveRetornarOk() throws Exception {
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.empty());
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L)));
        when(receitaService.editar(eq(10L), any(), eq(1L), eq("user_teste"))).thenReturn(new Receita());

        mockMvc.perform(put("/receitas/editar/10")
                        .with(csrf())
                        .param("nome", "Salário Atualizado")
                        .param("valor", "5200.00")
                        .param("dataLancamento", "2026-09-05")
                        .param("competencia", "2026-09")
                        .param("contaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(receitaService).editar(eq(10L), any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("BDD 16.4 - Registrar recebimento de receita de outro usuário retorna 404")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_REGISTRAR_RECEBIMENTO")
    void marcarRecebida_quandoReceitaDeOutroUsuario_deveRetornar404() throws Exception {
        when(receitaService.marcarRecebida(eq(70L), any(), eq(1L), eq("user_teste")))
                .thenThrow(new br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException("msg.receita.nao-encontrada"));

        mockMvc.perform(put("/receitas/marcar-recebida/70")
                        .with(csrf())
                        .param("dataRecebimento", "2026-10-05"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BDD 16.10 - Registrar recebimento com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_REGISTRAR_RECEBIMENTO")
    void marcarRecebida_comDataValida_deveRetornarOk() throws Exception {
        when(receitaService.marcarRecebida(eq(10L), eq(LocalDate.of(2026, 10, 5)), eq(1L), eq("user_teste")))
                .thenReturn(new Receita());

        mockMvc.perform(put("/receitas/marcar-recebida/10")
                        .with(csrf())
                        .param("dataRecebimento", "2026-10-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("RT09 / MSG02 - Registrar recebimento sem data retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_REGISTRAR_RECEBIMENTO")
    void marcarRecebida_semData_deveRetornar422() throws Exception {
        mockMvc.perform(put("/receitas/marcar-recebida/10")
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errosCampos.dataRecebimento").exists());

        verify(receitaService, never()).marcarRecebida(any(), any(), any(), any());
    }

    @Test
    @DisplayName("BDD 16.4 - Excluir receita de outro usuário retorna 404")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EXCLUIR")
    void excluir_quandoReceitaDeOutroUsuario_deveRetornar404() throws Exception {
        doThrow(new br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException("msg.receita.nao-encontrada"))
                .when(receitaService).excluir(70L, 1L, "user_teste");

        mockMvc.perform(delete("/receitas/excluir/70").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BDD 16.13 - Excluir receita manual com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EXCLUIR")
    void excluir_receitaManual_deveRetornarOk() throws Exception {
        mockMvc.perform(delete("/receitas/excluir/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(receitaService).excluir(10L, 1L, "user_teste");
    }

    @Test
    @DisplayName("BDD 16.14 - Excluir receita do Open Finance retorna 422 (MSG12)")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_EXCLUIR")
    void excluir_receitaOpenFinance_deveRetornar422() throws Exception {
        doThrow(new br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException("msg.receita.importada.nao-excluivel"))
                .when(receitaService).excluir(10L, 1L, "user_teste");

        mockMvc.perform(delete("/receitas/excluir/10").with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    @DisplayName("EDP08 / RN15 - Duplicar sem IDs retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void duplicar_semIds_deveRetornar422() throws Exception {
        mockMvc.perform(post("/receitas/duplicar").with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosNegocio.ids").exists());

        verify(receitaService, never()).duplicar(any(), any(), any(), any());
    }

    @Test
    @DisplayName("EDP08 / RN15 - Duplicar com sucesso retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_INSERIR")
    void duplicar_comSucesso_deveRetornar200() throws Exception {
        mockMvc.perform(post("/receitas/duplicar")
                        .param("ids", "1", "2")
                        .param("competenciaAlvo", "2026-10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").exists());

        verify(receitaService).duplicar(eq(List.of(1L, 2L)), eq("2026-10"), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("EDP08 / RN01 - Duplicar sem permissão retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_RECEITAS_LISTAR")
    void duplicar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(post("/receitas/duplicar")
                        .param("ids", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
