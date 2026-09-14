package br.com.diegocordeiro.dscproject.web.sistema.controller.categoria;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaGridDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.categoria.CategoriaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@Import(SecurityConfig.class)
class CategoriaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("BDD 16.0 - Listar categorias com autoridade PERM_CATEGORIAS_LISTAR")
    @WithMockUser(authorities = "PERM_CATEGORIAS_LISTAR")
    void listar_comPermissao_deveRetornarStatus200EViewListar() throws Exception {
        when(categoriaService.listar(any())).thenReturn(List.of(
            CategoriaGridDTO.builder()
                .id(1L)
                .codigo("SALARIO")
                .nome("Salário")
                .aplicaA(AplicaA.RECEITA)
                .sistema(true)
                .ativo(true)
                .qtdUso(0)
                .build()
        ));

        mockMvc.perform(get("/categorias/listar"))
            .andExpect(status().isOk())
            .andExpect(view().name("sistema/modulos/categoria/listar"))
            .andExpect(model().attributeExists("categorias"))
            .andExpect(model().attributeExists("aplicaAOpcoes"));
    }

    @Test
    @DisplayName("BDD 16.1 - Bloquear acesso de usuário sem permissão (403)")
    @WithMockUser(authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/categorias/listar"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("BDD 16.2 - Cadastrar categoria com sucesso")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_INSERIR"})
    void inserir_comDadosValidos_deveRedirecionarComMensagemSucesso() throws Exception {
        when(categoriaRepository.contarPorCodigo(eq("PETS"), any())).thenReturn(0L);
        when(categoriaService.inserir(any())).thenReturn(new Categoria());

        mockMvc.perform(post("/categorias/inserir")
                .with(csrf())
                .param("codigo", "PETS")
                .param("nome", "Pets")
                .param("aplicaA", "DESPESA"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));

        verify(categoriaService).inserir(any());
    }

    @Test
    @DisplayName("BDD 16.4 - Código de categoria duplicado re-renderiza com erro")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_INSERIR"})
    void inserir_quandoCodigoDuplicado_deveRenderizarComErro() throws Exception {
        when(categoriaRepository.contarPorCodigo(eq("ALIMENTACAO"), any())).thenReturn(1L);

        mockMvc.perform(post("/categorias/inserir")
                .with(csrf())
                .param("codigo", "ALIMENTACAO")
                .param("nome", "Alimentação")
                .param("aplicaA", "DESPESA"))
            .andExpect(status().isOk())
            .andExpect(view().name("sistema/modulos/categoria/listar"))
            .andExpect(model().attributeHasFieldErrors("categoriaForm", "codigo"));

        verify(categoriaService, never()).inserir(any());
    }

    @Test
    @DisplayName("BDD 16.6 - Editar categoria com sucesso")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_EDITAR"})
    void editar_comDadosValidos_deveRedirecionarComSucesso() throws Exception {
        when(categoriaRepository.contarPorCodigo(eq("LAZER"), eq(2L))).thenReturn(0L);
        when(categoriaService.editar(eq(2L), any())).thenReturn(new Categoria());

        mockMvc.perform(put("/categorias/editar/2")
                .with(csrf())
                .param("codigo", "LAZER")
                .param("nome", "Lazer e Hobbies")
                .param("aplicaA", "DESPESA")
                .param("cor", "#112233")
                .param("ativo", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));
    }

    @Test
    @DisplayName("BDD 16.7 - Não excluir categoria de sistema")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_EXCLUIR"})
    void excluir_categoriaSistema_deveRedirecionarComMensagemErro() throws Exception {
        doThrow(new RegraNegocioException("categoria.sistema.nao-excluivel"))
            .when(categoriaService).excluir(eq(1L), anyString());

        mockMvc.perform(delete("/categorias/excluir/1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias/listar"))
            .andExpect(flash().attributeExists("erroMensagem"));
    }

    @Test
    @DisplayName("BDD 16.8 - Não excluir categoria em uso quando parâmetro bloqueia")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_EXCLUIR"})
    void excluir_categoriaEmUso_deveRedirecionarComErroEOfertaDesativar() throws Exception {
        doThrow(new RegraNegocioException("categoria.em-uso.bloqueada"))
            .when(categoriaService).excluir(eq(3L), anyString());

        mockMvc.perform(delete("/categorias/excluir/3")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias/listar"))
            .andExpect(flash().attributeExists("erroMensagem"))
            .andExpect(flash().attribute("oferecerDesativarId", 3L));
    }

    @Test
    @DisplayName("BDD 16.9 - Desativar categoria")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_EDITAR"})
    void desativar_deveChamarServicoERedirecionar() throws Exception {
        mockMvc.perform(put("/categorias/desativar/4")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));

        verify(categoriaService).desativar(4L);
    }

    @Test
    @DisplayName("BDD 16.10 - Excluir categoria comum sem uso com sucesso")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_LISTAR", "PERM_CATEGORIAS_EXCLUIR"})
    void excluir_categoriaComumSemUso_deveRedirecionarComSucesso() throws Exception {
        mockMvc.perform(delete("/categorias/excluir/5")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));

        verify(categoriaService).excluir(eq(5L), anyString());
    }

    @Test
    @DisplayName("BDD 16.11 - Combobox de lançamento filtra por aplicaA em JSON")
    @WithMockUser
    void opcoesCombobox_autenticado_deveRetornarListaJson() throws Exception {
        when(categoriaService.listarOpcoesCombobox(AplicaA.DESPESA)).thenReturn(List.of(
            new CategoriaOpcaoDTO(1L, "ALUGUEL", "Aluguel", "#ffffff", "home")
        ));

        mockMvc.perform(get("/categorias/opcoes?aplicaA=DESPESA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("ALUGUEL"))
            .andExpect(jsonPath("$[0].nome").value("Aluguel"));
    }
}
