package br.com.diegocordeiro.dscproject.config;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class SecurityConfigTest {

    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @BeforeEach
    void setup(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void acessoRaiz_semAutenticacao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void paginaLogin_semAutenticacao_retornaOk() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void webjars_semAutenticacao_naoRedirecionaParaLogin() throws Exception {
        var result = mockMvc.perform(get("/webjars/tabler__core/1.4.0/dist/css/tabler.min.css")).andReturn();
        assertThat(result.getResponse().getStatus()).isNotEqualTo(302);
    }

    // ---------- Rotas públicas do módulo (RN01) ----------

    @Test
    void autoCadastro_get_semAutenticacao_naoRedireciona() throws Exception {
        var result = mockMvc.perform(get("/usuarios/cadastrar-site")).andReturn();
        assertThat(result.getResponse().getStatus()).isNotEqualTo(302);
    }

    @Test
    void recuperarSenha_get_semAutenticacao_naoRedireciona() throws Exception {
        var result = mockMvc.perform(get("/usuarios/recuperar-senha")).andReturn();
        assertThat(result.getResponse().getStatus()).isNotEqualTo(302);
    }

    @Test
    void existe_semAutenticacao_retornaOk() throws Exception {
        when(usuarioService.verificarSeExiste("qualquer", null)).thenReturn(false);
        mockMvc.perform(get("/usuarios/existe").param("valor", "qualquer"))
            .andExpect(status().isOk());
    }

    // ---------- BDD 16.1 / 16.13 — autorização por operação ----------

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listarDados_perfilUser_403() throws Exception {
        mockMvc.perform(get("/usuarios/listar-dados")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"PERM_USUARIOS_LISTAR", "PERM_USUARIOS_VER_HISTORICO"})
    void excluir_semPermissaoExcluir_403() throws Exception {
        mockMvc.perform(delete("/usuarios/excluir/1").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"PERM_USUARIOS_LISTAR"})
    void alterarSenha_semPermissaoEditar_403() throws Exception {
        mockMvc.perform(put("/usuarios/1/senha").with(csrf())
                .param("senha", "senha123").param("confirmacaoSenha", "senha123"))
            .andExpect(status().isForbidden());
    }

    // ---------- Perfis e Permissões — autorização por operação (RN01) ----------

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void perfisListarDados_semPermissao_403() throws Exception {
        mockMvc.perform(get("/perfis/listar-dados")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void perfisBuscar_soComListar_403() throws Exception {
        mockMvc.perform(get("/perfis/buscar/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EDITAR")
    void sincronizarCatalogo_semPermissaoSincronizar_403() throws Exception {
        mockMvc.perform(post("/permissoes/sincronizar-catalogo").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void permissoesListarDados_comListar_naoRecebe403() throws Exception {
        var status = mockMvc.perform(get("/permissoes/listar-dados")).andReturn().getResponse().getStatus();
        assertThat(status).isNotEqualTo(403);
    }

    // ---------- Parâmetros Globais — autorização por operação (RN01) ----------

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void parametrosListarDados_semPermissao_403() throws Exception {
        mockMvc.perform(get("/parametros/listar-dados")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void parametrosEditar_soComListar_403() throws Exception {
        mockMvc.perform(put("/parametros/editar/1").with(csrf())
                .param("valor", "x").param("motivo", "y"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void parametrosHistorico_comListar_naoRecebe403() throws Exception {
        var status = mockMvc.perform(get("/parametros/historico/1")).andReturn().getResponse().getStatus();
        assertThat(status).isNotEqualTo(403);
    }

    // ---------- RN19 — Configurações da Conta exige apenas autenticação ----------

    @Test
    void minhaConta_semAutenticacao_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/minha-conta"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "diego", authorities = "ROLE_USER")
    void minhaConta_usuarioComum_naoRecebe403() throws Exception {
        when(usuarioService.buscarPorLogin("diego"))
            .thenReturn(br.com.diegocordeiro.dscproject.support.TestFixtures.usuario(
                1L, "diego", br.com.diegocordeiro.dscproject.support.TestFixtures.perfil("USER")));

        var status = mockMvc.perform(get("/minha-conta")).andReturn().getResponse().getStatus();
        assertThat(status).isNotEqualTo(403);
    }
}
