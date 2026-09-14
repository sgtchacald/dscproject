package br.com.diegocordeiro.dscproject.web.sistema.controller.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.instituicaofinanceira.InstituicaoFinanceiraService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstituicaoFinanceiraController.class)
@Import(SecurityConfig.class)
class InstituicaoFinanceiraControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private InstituicaoFinanceiraService instituicaoFinanceiraService;

    @MockitoBean
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("Listar view com autoridade PERM_INSTITUICOES_LISTAR")
    @WithMockUser(authorities = "PERM_INSTITUICOES_LISTAR")
    void listar_comPermissao_deveRetornar200EViewListar() throws Exception {
        mockMvc.perform(get("/instituicoes-financeiras/listar"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/instituicao-financeira/listar"))
                .andExpect(model().attributeExists("tipos"));
    }

    @Test
    @DisplayName("Bloquear acesso de usuário sem permissão à listagem")
    @WithMockUser(authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/instituicoes-financeiras/listar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Listar dados JSON com permissão")
    @WithMockUser(authorities = "PERM_INSTITUICOES_LISTAR")
    void listarDados_comPermissao_deveRetornarJsonGrid() throws Exception {
        when(instituicaoFinanceiraService.listarParaGrid()).thenReturn(List.of(
                InstituicaoFinanceiraGridDTO.builder()
                        .id(1L)
                        .nome("Banco do Brasil")
                        .codigo("001")
                        .tipo(TipoInstituicaoFinanceira.BANCO)
                        .sistema(true)
                        .ativo(true)
                        .build()
        ));

        mockMvc.perform(get("/instituicoes-financeiras/listar-dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Banco do Brasil"));
    }

    @Test
    @DisplayName("Buscar dados para edição")
    @WithMockUser(authorities = "PERM_INSTITUICOES_EDITAR")
    void buscar_comPermissao_deveRetornarEdicaoDTO() throws Exception {
        InstituicaoFinanceiraEdicaoDTO dto = new InstituicaoFinanceiraEdicaoDTO();
        dto.setId(1L);
        dto.setNome("Banco do Brasil");
        dto.setCodigo("001");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);
        dto.setSistema(true);
        dto.setAtivo(true);

        when(instituicaoFinanceiraService.buscarParaEdicao(1L)).thenReturn(dto);

        mockMvc.perform(get("/instituicoes-financeiras/buscar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Banco do Brasil"))
                .andExpect(jsonPath("$.tipo").value("BANCO"));
    }

    @Test
    @DisplayName("Inserir instituição com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_INSERIR")
    void inserir_comDadosValidos_deveRetornarOk() throws Exception {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Inovador", null)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.contarPorCodigo("777", null)).thenReturn(0L);
        when(instituicaoFinanceiraService.inserir(any())).thenReturn(new InstituicaoFinanceira());

        mockMvc.perform(post("/instituicoes-financeiras/inserir")
                        .with(csrf())
                        .param("nome", "Banco Inovador")
                        .param("codigo", "777")
                        .param("tipo", "BANCO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoFinanceiraService).inserir(any());
    }

    @Test
    @DisplayName("Inserir instituição com nome duplicado retorna 422")
    @WithMockUser(authorities = "PERM_INSTITUICOES_INSERIR")
    void inserir_quandoNomeDuplicado_deveRetornar422() throws Exception {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Inovador", null)).thenReturn(1L);

        mockMvc.perform(post("/instituicoes-financeiras/inserir")
                        .with(csrf())
                        .param("nome", "Banco Inovador")
                        .param("codigo", "777")
                        .param("tipo", "BANCO"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosNegocio.nome").exists());

        verify(instituicaoFinanceiraService, never()).inserir(any());
    }

    @Test
    @DisplayName("Editar instituição com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_EDITAR")
    void editar_comDadosValidos_deveRetornarOk() throws Exception {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Atualizado", 2L)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.contarPorCodigo("888", 2L)).thenReturn(0L);

        mockMvc.perform(put("/instituicoes-financeiras/editar/2")
                        .with(csrf())
                        .param("nome", "Banco Atualizado")
                        .param("codigo", "888")
                        .param("tipo", "BANCO")
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoFinanceiraService).editar(eq(2L), any());
    }

    @Test
    @DisplayName("Desativar instituição com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_EDITAR")
    void desativar_comPermissao_deveRetornarOk() throws Exception {
        mockMvc.perform(put("/instituicoes-financeiras/desativar/3")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoFinanceiraService).desativar(3L);
    }

    @Test
    @DisplayName("Excluir instituição com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_EXCLUIR", username = "admin_user")
    void excluir_comPermissao_deveRetornarOk() throws Exception {
        mockMvc.perform(delete("/instituicoes-financeiras/excluir/4")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoFinanceiraService).excluir(eq(4L), eq("admin_user"));
    }

    @Test
    @DisplayName("Listar opções em combobox por usuário autenticado")
    @WithMockUser
    void listarOpcoes_usuarioAutenticado_deveRetornarListaJson() throws Exception {
        when(instituicaoFinanceiraService.listarOpcoesCombobox(TipoInstituicaoFinanceira.BANCO)).thenReturn(List.of(
                new InstituicaoFinanceiraOpcaoDTO(1L, "Banco do Brasil", "001", TipoInstituicaoFinanceira.BANCO)
        ));

        mockMvc.perform(get("/instituicoes-financeiras/opcoes?tipo=BANCO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Banco do Brasil"))
                .andExpect(jsonPath("$[0].codigo").value("001"));
    }

    @Test
    @DisplayName("Listar opções por usuário não autenticado redireciona para login")
    void listarOpcoes_usuarioNaoAutenticado_deveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/instituicoes-financeiras/opcoes"))
                .andExpect(status().is3xxRedirection());
    }
}
