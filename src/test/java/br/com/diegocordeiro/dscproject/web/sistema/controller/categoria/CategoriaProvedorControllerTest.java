package br.com.diegocordeiro.dscproject.web.sistema.controller.categoria;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorGridDTO;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.categoria.CategoriaProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.categoria.CategoriaProvedorService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaProvedorController.class)
@Import(SecurityConfig.class)
class CategoriaProvedorControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaProvedorService categoriaProvedorService;

    @MockitoBean
    private CategoriaProvedorRepository categoriaProvedorRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private OpfiProvedorRepository opfiProvedorRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("BDD 16.0 / Categorias por provedor - Listar com permissão")
    @WithMockUser(authorities = "PERM_CATEGORIAS_PROVEDOR_LISTAR")
    void listar_comPermissao_deveRetornar200() throws Exception {
        when(categoriaProvedorService.listar(null)).thenReturn(List.of(
            CategoriaProvedorGridDTO.builder()
                .id(1L)
                .rotuloExterno("Food")
                .provedorId(1L)
                .provedorNome("Pluggy")
                .categoriaId(10L)
                .categoriaNome("Alimentação")
                .build()
        ));

        mockMvc.perform(get("/categorias-provedor/listar"))
            .andExpect(status().isOk())
            .andExpect(view().name("sistema/modulos/categoria-provedor/listar"))
            .andExpect(model().attributeExists("vinculos"));
    }

    @Test
    @DisplayName("BDD 16.1 - Bloquear acesso de usuário sem permissão")
    @WithMockUser(authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/categorias-provedor/listar"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("BDD 16.12 - Cadastrar vínculo categoria x provedor com sucesso")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_PROVEDOR_LISTAR", "PERM_CATEGORIAS_PROVEDOR_INSERIR"})
    void inserir_comDadosValidos_deveRedirecionar() throws Exception {
        when(categoriaProvedorRepository.contarPorProvedorERotulo(eq(1L), eq("Food and drinks"), any())).thenReturn(0L);

        Categoria c = new Categoria();
        c.setId(10L);
        c.setAtivo(true);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(c));

        OpfiProvedor p = new OpfiProvedor();
        p.setId(1L);
        p.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(p));

        when(categoriaProvedorService.inserir(any())).thenReturn(new CategoriaProvedor());

        mockMvc.perform(post("/categorias-provedor/inserir")
                .with(csrf())
                .param("provedorId", "1")
                .param("rotuloExterno", "Food and drinks")
                .param("categoriaId", "10"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias-provedor/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));

        verify(categoriaProvedorService).inserir(any());
    }

    @Test
    @DisplayName("Editar vínculo categoria x provedor com sucesso")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_PROVEDOR_LISTAR", "PERM_CATEGORIAS_PROVEDOR_EDITAR"})
    void editar_comDadosValidos_deveRedirecionar() throws Exception {
        when(categoriaProvedorRepository.contarPorProvedorERotulo(eq(1L), eq("Food and drinks"), eq(5L))).thenReturn(0L);

        Categoria c = new Categoria();
        c.setId(10L);
        c.setAtivo(true);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(c));

        OpfiProvedor p = new OpfiProvedor();
        p.setId(1L);
        p.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(p));

        when(categoriaProvedorService.editar(eq(5L), any())).thenReturn(new CategoriaProvedor());

        mockMvc.perform(put("/categorias-provedor/editar/5")
                .with(csrf())
                .param("provedorId", "1")
                .param("rotuloExterno", "Food and drinks")
                .param("categoriaId", "10"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias-provedor/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));
    }

    @Test
    @DisplayName("Excluir vínculo categoria x provedor com sucesso")
    @WithMockUser(authorities = {"PERM_CATEGORIAS_PROVEDOR_LISTAR", "PERM_CATEGORIAS_PROVEDOR_EXCLUIR"})
    void excluir_deveRedirecionar() throws Exception {
        mockMvc.perform(delete("/categorias-provedor/excluir/8")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categorias-provedor/listar"))
            .andExpect(flash().attributeExists("sucessoMensagem"));

        verify(categoriaProvedorService).excluir(eq(8L), any());
    }
}
