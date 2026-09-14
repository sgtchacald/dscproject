package br.com.diegocordeiro.dscproject.web.sistema.controller.perfil;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilResumoDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.perfil.PerfilService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerfilController.class)
@Import(SecurityConfig.class)
class PerfilControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private PerfilService perfilService;
    @MockitoBean
    private AutorizacaoService autorizacaoService;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setup(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static Perfil perfil(Long id, String codigo, boolean sistema) {
        Perfil p = new Perfil(codigo, codigo, null, sistema);
        p.setId(id);
        return p;
    }

    // ---------- RN01 / BDD 16.0 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void listarDados_comPermissaoListar_200() throws Exception {
        when(perfilService.listar()).thenReturn(List.of(
            new PerfilResumoDTO(1L, "ADMIN", "Administrador", true, 24, 2)));

        mockMvc.perform(get("/perfis/listar-dados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("ADMIN"))
            .andExpect(jsonPath("$[0].qtdPermissoes").value(24));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listarDados_semPermissao_403() throws Exception {
        mockMvc.perform(get("/perfis/listar-dados")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void listar_renderizaPaginaComFragmentsColunaAcoesEJs() throws Exception {
        mockMvc.perform(get("/perfis/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("id=\"modalPerfil\""),
                org.hamcrest.Matchers.containsString("id=\"modalCatalogo\""),
                org.hamcrest.Matchers.containsString(">Ações<"),
                org.hamcrest.Matchers.containsString("<ol class=\"breadcrumb\">"),
                org.hamcrest.Matchers.containsString("/js/perfil/listar.js"))));
    }

    // ---------- RN01 — botões com permissão atômica ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void listar_semInserir_naoMostraBotaoNovoPerfil() throws Exception {
        mockMvc.perform(get("/perfis/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("id=\"btnNovoPerfil\""))));
    }

    @Test
    @WithMockUser(authorities = {"PERM_PERFIS_LISTAR", "PERM_PERFIS_INSERIR"})
    void listar_comInserir_mostraBotaoNovoPerfil() throws Exception {
        mockMvc.perform(get("/perfis/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"btnNovoPerfil\"")));
    }

    // ---------- EDP03 / BDD 16.3 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EDITAR")
    void buscar_perfilDeSistema_devolveFlagSistema() throws Exception {
        when(perfilService.buscarParaEdicao(1L))
            .thenReturn(new PerfilEdicaoDTO(perfil(1L, "ADMIN", true), List.of("PERFIS_EDITAR")));

        mockMvc.perform(get("/perfis/buscar/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sistema").value(true))
            .andExpect(jsonPath("$.permissoes[0]").value("PERFIS_EDITAR"));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void buscar_semManter_403() throws Exception {
        mockMvc.perform(get("/perfis/buscar/1")).andExpect(status().isForbidden());
    }

    // ---------- EDP05 / BDD 16.1 e 16.2 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_INSERIR")
    void inserir_dadosValidos_200() throws Exception {
        when(perfilService.verificarCodigoDuplicado(any(), any())).thenReturn(false);
        when(perfilService.inserir(any())).thenReturn(new Perfil());

        mockMvc.perform(post("/perfis/inserir").with(csrf())
                .param("codigo", "RELATORIOS").param("nome", "Relatórios")
                .param("permissoes", "USUARIOS_LISTAR").param("permissoes", "DESPESAS_LISTAR")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_INSERIR")
    void inserir_codigoDuplicado_422ComMsg04() throws Exception {
        when(perfilService.verificarCodigoDuplicado(eq("ADMIN"), isNull())).thenReturn(true);

        mockMvc.perform(post("/perfis/inserir").with(csrf())
                .param("codigo", "ADMIN").param("nome", "Administrador 2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosNegocio.codigo").value("Já existe um perfil com este código."));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_INSERIR")
    void inserir_semNome_422ErroDeCampo() throws Exception {
        mockMvc.perform(post("/perfis/inserir").with(csrf())
                .param("codigo", "RELATORIOS").param("nome", "")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosCampos.nome").exists());
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void inserir_semManter_403() throws Exception {
        mockMvc.perform(post("/perfis/inserir").with(csrf())
                .param("codigo", "X").param("nome", "X"))
            .andExpect(status().isForbidden());
    }

    // ---------- EDP06 / RN05 e RN06 / BDD 16.4 e 16.5 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EDITAR")
    void editar_antiLockoutGlobal_422ComMsg06() throws Exception {
        when(perfilService.verificarCodigoDuplicado(any(), any())).thenReturn(false);
        doThrow(new RegraNegocioException("perfil.antilockout.global"))
            .when(perfilService).editar(eq(1L), any(), any());

        mockMvc.perform(put("/perfis/editar/1").with(csrf())
                .param("codigo", "ADMIN").param("nome", "Administrador")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem")
                .value("Esta alteração deixaria o sistema sem nenhum perfil capaz de gerenciar perfis ou usuários."));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EDITAR")
    void editar_antiLockoutProprioPerfil_422ComMsg05() throws Exception {
        when(perfilService.verificarCodigoDuplicado(any(), any())).thenReturn(false);
        doThrow(new RegraNegocioException("perfil.antilockout.proprio"))
            .when(perfilService).editar(eq(1L), any(), any());

        mockMvc.perform(put("/perfis/editar/1").with(csrf())
                .param("codigo", "ADMIN").param("nome", "Administrador")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem")
                .value("Você não pode remover a permissão de gerenciar perfis do seu próprio perfil."));
    }

    // ---------- EDP07 / RN07 / BDD 16.6 e 16.7 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EXCLUIR")
    void excluir_perfilDeSistema_422ComMsg08() throws Exception {
        doThrow(new RegraNegocioException("perfil.exclusao.sistema")).when(perfilService).excluir(2L);

        mockMvc.perform(delete("/perfis/excluir/2").with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Perfis de sistema não podem ser excluídos."));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EXCLUIR")
    void excluir_perfilComUsuarios_422ComMsg09() throws Exception {
        doThrow(new RegraNegocioException("perfil.exclusao.com.usuarios")).when(perfilService).excluir(9L);

        mockMvc.perform(delete("/perfis/excluir/9").with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("usuários vinculados")));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EXCLUIR")
    void excluir_ok_200ComMsg10() throws Exception {
        mockMvc.perform(delete("/perfis/excluir/9").with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.mensagem").value("Perfil excluído com sucesso."));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void excluir_semManter_403() throws Exception {
        mockMvc.perform(delete("/perfis/excluir/9").with(csrf())).andExpect(status().isForbidden());
    }
}
