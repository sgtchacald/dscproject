package br.com.diegocordeiro.dscproject.web.sistema.controller.usuario;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.usuario.RecuperacaoSenhaService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioRedeSocialService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
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

import static br.com.diegocordeiro.dscproject.support.TestFixtures.perfil;
import static br.com.diegocordeiro.dscproject.support.TestFixtures.usuario;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class MinhaContaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;
    @MockitoBean
    private RecuperacaoSenhaService recuperacaoSenhaService;
    @MockitoBean
    private UsuarioRedeSocialService usuarioRedeSocialService;
    @MockitoBean
    private AutorizacaoService autorizacaoService;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setup(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ---------- EDP13 — página vem preenchida, sem senha ----------

    @Test
    @WithMockUser(username = "diego")
    void getMinhaConta_autenticado_renderizaComOsDadosDoUsuario() throws Exception {
        when(usuarioService.buscarPorLogin("diego")).thenReturn(usuario(1L, "diego", perfil("USER")));

        mockMvc.perform(get("/minha-conta"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("Configurações da Conta"),
                org.hamcrest.Matchers.containsString("id=\"formMinhaConta\""),
                org.hamcrest.Matchers.containsString("Meus Dados"),
                org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("name=\"perfilCodigo\"")))));
    }

    @Test
    void getMinhaConta_naoAutenticado_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/minha-conta"))
            .andExpect(status().is3xxRedirection());
    }

    // ---------- BDD 16.16 — atualiza dados sem trocar a senha ----------

    @Test
    @WithMockUser(username = "diego")
    void putMinhaConta_semSenha_200ComMsg22() throws Exception {
        when(usuarioService.buscarPorLogin("diego")).thenReturn(usuario(9L, "diego", perfil("USER")));
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.atualizarPropriaConta(eq("diego"), any())).thenReturn(new Usuario());

        mockMvc.perform(put("/minha-conta").with(csrf())
                .param("nome", "Diego Atualizado").param("genero", "M").param("nascimento", "1986-05-20")
                .param("email", "diego@test.com").param("login", "diego")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.mensagem").value("Dados atualizados com sucesso."));
    }

    @Test
    @WithMockUser(username = "diego")
    void putMinhaConta_camposDeSenhaVaziosNoFormulario_naoValidaSenha() throws Exception {
        when(usuarioService.buscarPorLogin("diego")).thenReturn(usuario(9L, "diego", perfil("USER")));
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.atualizarPropriaConta(eq("diego"), any())).thenReturn(new Usuario());

        mockMvc.perform(put("/minha-conta").with(csrf())
                .param("nome", "Diego").param("genero", "M").param("nascimento", "1986-05-20")
                .param("email", "diego@test.com").param("login", "diego")
                .param("senha", "").param("confirmacaoSenha", "")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    // ---------- BDD 16.17 — troca a própria senha ----------

    @Test
    @WithMockUser(username = "diego")
    void putMinhaConta_comNovaSenha_200() throws Exception {
        when(usuarioService.buscarPorLogin("diego")).thenReturn(usuario(9L, "diego", perfil("USER")));
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.atualizarPropriaConta(eq("diego"), any())).thenReturn(new Usuario());

        mockMvc.perform(put("/minha-conta").with(csrf())
                .param("nome", "Diego").param("genero", "M").param("nascimento", "1986-05-20")
                .param("email", "diego@test.com").param("login", "diego")
                .param("senha", "senhaNova").param("confirmacaoSenha", "senhaNova")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    // ---------- BDD 16.18 — perfil enviado na requisição é ignorado ----------

    @Test
    @WithMockUser(username = "diego")
    void putMinhaConta_perfilEnviado_ehIgnorado() throws Exception {
        when(usuarioService.buscarPorLogin("diego")).thenReturn(usuario(9L, "diego", perfil("USER")));
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.atualizarPropriaConta(eq("diego"), any())).thenReturn(new Usuario());

        mockMvc.perform(put("/minha-conta").with(csrf())
                .param("nome", "Diego").param("genero", "M").param("nascimento", "1986-05-20")
                .param("email", "diego@test.com").param("login", "diego")
                .param("perfilCodigo", "ADMIN")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(usuarioService).atualizarPropriaConta(eq("diego"), any());
    }

    // ---------- BDD 16.19 — e-mail já usado por outro ----------

    @Test
    @WithMockUser(username = "diego")
    void putMinhaConta_emailDeOutroUsuario_422ComMsg04() throws Exception {
        when(usuarioService.buscarPorLogin("diego")).thenReturn(usuario(9L, "diego", perfil("USER")));
        when(usuarioService.verificarSeExiste(eq("diego"), eq(9L))).thenReturn(false);
        when(usuarioService.verificarSeExiste(eq("ana@x.com"), eq(9L))).thenReturn(true);

        mockMvc.perform(put("/minha-conta").with(csrf())
                .param("nome", "Diego").param("genero", "M").param("nascimento", "1986-05-20")
                .param("email", "ana@x.com").param("login", "diego")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosNegocio.email").value("Este e-mail já está em uso."));
    }
}
