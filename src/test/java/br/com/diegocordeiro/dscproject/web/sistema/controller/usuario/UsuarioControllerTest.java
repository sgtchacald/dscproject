package br.com.diegocordeiro.dscproject.web.sistema.controller.usuario;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioListaDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialDTO;
import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.usuario.RecuperacaoSenhaService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioRedeSocialService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
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

import static br.com.diegocordeiro.dscproject.support.TestFixtures.perfil;
import static br.com.diegocordeiro.dscproject.support.TestFixtures.usuario;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

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

    // ---------- BDD 16.0 — listar sem senha ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_LISTAR")
    void listarDados_comPermissao_retornaGridSemSenha() throws Exception {
        Usuario u = usuario(1L, "diego", perfil("ADMIN"));
        when(usuarioService.listarParaGrid()).thenReturn(List.of(new UsuarioListaDTO(u)));

        mockMvc.perform(get("/usuarios/listar-dados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].login").value("diego"))
            .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    // ---------- BDD 16.1 — bloquear sem permissão ----------

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listarDados_perfilSemPermissao_403() throws Exception {
        mockMvc.perform(get("/usuarios/listar-dados")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listar_perfilSemPermissao_403() throws Exception {
        mockMvc.perform(get("/usuarios/listar")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_LISTAR")
    void listar_comPermissao_renderizaPaginaComOsFragmentsDasModais() throws Exception {
        mockMvc.perform(get("/usuarios/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("id=\"modalFiltro\""),
                org.hamcrest.Matchers.containsString("id=\"modalUsuario\""),
                org.hamcrest.Matchers.containsString("id=\"modalHistorico\""),
                org.hamcrest.Matchers.containsString("/js/usuario/listar.js"))));
    }

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_LISTAR")
    void listar_renderizaCabecalhoNoLugarDoDashboardComBreadcrumb() throws Exception {
        mockMvc.perform(get("/usuarios/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("Gerenciar Usuários"),
                org.hamcrest.Matchers.containsString("<ol class=\"breadcrumb\">"),
                org.hamcrest.Matchers.containsString("aria-current=\"page\""),
                org.hamcrest.Matchers.containsString(">Ações<"),
                org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(">Dashboard<")))));
    }

    // ---------- BDD 16.2 — cadastrar ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_INSERIR")
    void inserir_dadosValidos_retornaSucesso() throws Exception {
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.inserir(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/usuarios/inserir").with(csrf())
                .param("nome", "João Silva")
                .param("genero", "M")
                .param("nascimento", "1990-01-01")
                .param("email", "joao@test.com")
                .param("login", "joaosilva")
                .param("senha", "senha123")
                .param("confirmacaoSenha", "senha123")
                .param("perfilCodigo", "USER")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void inserir_semPermissao_403() throws Exception {
        mockMvc.perform(post("/usuarios/inserir").with(csrf())
                .param("nome", "x").param("genero", "M").param("nascimento", "1990-01-01")
                .param("email", "j@test.com").param("login", "joao").param("senha", "senha123")
                .param("confirmacaoSenha", "senha123"))
            .andExpect(status().isForbidden());
    }

    // ---------- BDD 16.3 — login duplicado ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_INSERIR")
    void inserir_loginDuplicado_422ComMsg03() throws Exception {
        when(usuarioService.verificarSeExiste(eq("joaosilva"), any())).thenReturn(true);
        when(usuarioService.verificarSeExiste(eq("joao@test.com"), any())).thenReturn(false);

        mockMvc.perform(post("/usuarios/inserir").with(csrf())
                .param("nome", "João Silva").param("genero", "M").param("nascimento", "1990-01-01")
                .param("email", "joao@test.com").param("login", "joaosilva")
                .param("senha", "senha123").param("confirmacaoSenha", "senha123")
                .param("perfilCodigo", "USER")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosNegocio.login").value("Este login já está em uso."));
    }

    // ---------- BDD 16.4 — confirmação diferente ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_INSERIR")
    void inserir_senhasDiferentes_422ComMsg07() throws Exception {
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);

        mockMvc.perform(post("/usuarios/inserir").with(csrf())
                .param("nome", "João Silva").param("genero", "M").param("nascimento", "1990-01-01")
                .param("email", "joao@test.com").param("login", "joaosilva")
                .param("senha", "senha123").param("confirmacaoSenha", "outrasenha")
                .param("perfilCodigo", "USER")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosNegocio.confirmacaoSenha").value("As senhas não conferem."));
    }

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_INSERIR")
    void inserir_nomeFaltando_422ErroDeCampo() throws Exception {
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);

        mockMvc.perform(post("/usuarios/inserir").with(csrf())
                .param("nome", "").param("genero", "M").param("nascimento", "1990-01-01")
                .param("email", "joao@test.com").param("login", "joaosilva")
                .param("senha", "senha123").param("confirmacaoSenha", "senha123")
                .param("perfilCodigo", "USER")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosCampos.nome").exists());
    }

    // ---------- BDD 16.5 — editar sem trocar a senha ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_EDITAR")
    void editar_semSenha_retornaSucesso() throws Exception {
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.editar(eq(5L), any())).thenReturn(new Usuario());

        mockMvc.perform(put("/usuarios/editar/5").with(csrf())
                .param("nome", "João Silva").param("genero", "M").param("nascimento", "1990-01-01")
                .param("email", "joao@test.com").param("login", "joaosilva")
                .param("perfilCodigo", "USER")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    // ---------- EDP03 / RN03 — buscar não devolve senha ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_EDITAR")
    void buscar_naoIncluiSenhaNoRetorno() throws Exception {
        when(usuarioService.buscarParaEdicao(5L))
            .thenReturn(new br.com.diegocordeiro.dscproject.dto.usuario.UsuarioEdicaoDTO(
                usuario(5L, "diego", perfil("ADMIN"))));

        mockMvc.perform(get("/usuarios/buscar/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("diego"))
            .andExpect(jsonPath("$.senha").doesNotExist());
    }

    // ---------- BDD 16.7 — não excluir a si mesmo ----------

    @Test
    @WithMockUser(username = "diego", authorities = "PERM_USUARIOS_EXCLUIR")
    void excluir_proprioUsuario_422ComMsg10() throws Exception {
        doThrow(new RegraNegocioException("usuario.exclusao.proprio"))
            .when(usuarioService).excluir(eq(5L), any());

        mockMvc.perform(delete("/usuarios/excluir/5").with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Você não pode excluir o seu próprio usuário."));
    }

    // ---------- BDD 16.8 — não excluir o último ADMIN ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_EXCLUIR")
    void excluir_ultimoAdmin_422ComMsg11() throws Exception {
        doThrow(new RegraNegocioException("usuario.ultimo.admin"))
            .when(usuarioService).excluir(eq(7L), any());

        mockMvc.perform(delete("/usuarios/excluir/7").with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem")
                .value("Não é possível excluir ou rebaixar o último usuário administrador ativo."));
    }

    // ---------- BDD 16.13 — permissão parcial ----------

    @Test
    @WithMockUser(authorities = {"PERM_USUARIOS_LISTAR", "PERM_USUARIOS_VER_HISTORICO"})
    void excluir_semPermissaoExcluir_403() throws Exception {
        mockMvc.perform(delete("/usuarios/excluir/3").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void historico_semPermissao_403() throws Exception {
        mockMvc.perform(get("/usuarios/historico/3")).andExpect(status().isForbidden());
    }

    // ---------- EDP07 — existe é público ----------

    @Test
    void existe_publicoSemAutenticacao_200() throws Exception {
        when(usuarioService.verificarSeExiste(eq("diego"), any())).thenReturn(true);

        mockMvc.perform(get("/usuarios/existe").param("valor", "diego"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));
    }

    // ---------- BDD 16.6 — auto-cadastro cria sempre USER ----------

    @Test
    void cadastrarSite_publico_ignoraPerfilEnviado() throws Exception {
        when(usuarioService.verificarSeExiste(any(), any())).thenReturn(false);
        when(usuarioService.autoCadastrar(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/usuarios/cadastrar-site").with(csrf())
                .param("nome", "Novo Usuário").param("genero", "F").param("nascimento", "2000-03-03")
                .param("email", "novo@test.com").param("login", "novousuario")
                .param("senha", "senha123").param("confirmacaoSenha", "senha123")
                .param("perfilCodigo", "ADMIN")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    // ---------- BDD 16.14 / EDP12 / RT13 — ADMIN altera a senha pela ação dedicada ----------

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_EDITAR")
    void alterarSenha_dadosValidos_200ComMsg21() throws Exception {
        when(usuarioService.alterarSenha(eq(5L), eq("senha123"))).thenReturn(new Usuario());

        mockMvc.perform(put("/usuarios/5/senha").with(csrf())
                .param("senha", "senha123")
                .param("confirmacaoSenha", "senha123")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.mensagem").value("Senha do usuário alterada com sucesso."));
    }

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_EDITAR")
    void alterarSenha_confirmacaoDiferente_422ComMsg07() throws Exception {
        mockMvc.perform(put("/usuarios/5/senha").with(csrf())
                .param("senha", "senha123")
                .param("confirmacaoSenha", "outrasenha")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosNegocio.confirmacaoSenha").value("As senhas não conferem."));
    }

    @Test
    @WithMockUser(authorities = "PERM_USUARIOS_EDITAR")
    void alterarSenha_senhaCurta_422ErroDeCampo() throws Exception {
        mockMvc.perform(put("/usuarios/5/senha").with(csrf())
                .param("senha", "123")
                .param("confirmacaoSenha", "123")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosCampos.senha").exists());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void alterarSenha_semPermissaoEditar_403() throws Exception {
        mockMvc.perform(put("/usuarios/5/senha").with(csrf())
                .param("senha", "senha123").param("confirmacaoSenha", "senha123"))
            .andExpect(status().isForbidden());
    }

    // ---------- BDD 16.15 — edição sem campo de senha; modal dedicado presente ----------

    @Test
    @WithMockUser(authorities = {"PERM_USUARIOS_LISTAR", "PERM_USUARIOS_EDITAR"})
    void listar_trazBlocoDeSenhaSeparadoEModalDedicado() throws Exception {
        mockMvc.perform(get("/usuarios/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("id=\"blocoSenha\""),
                org.hamcrest.Matchers.containsString("id=\"modalAlterarSenha\""),
                org.hamcrest.Matchers.containsString("data-perm=\"alterar-senha\""))));
    }

    // ---------- Redes Sociais do Usuário (EDP15 a EDP19) ----------

    @Test
    @WithMockUser(username = "admin")
    void listarRedesSociais_autenticado_retornaLista() throws Exception {
        Usuario user = usuario(1L, "admin", perfil("ADMIN"));
        when(usuarioService.buscarPorLogin("admin")).thenReturn(user);
        when(usuarioRedeSocialService.listarPorUsuario(1L)).thenReturn(List.of(
            UsuarioRedeSocialDTO.builder().id(10L).tipo(TipoRedeSocial.LINKEDIN).url("https://linkedin.com").ativo(true).build()
        ));

        mockMvc.perform(get("/minha-conta/redes-sociais").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(10L))
            .andExpect(jsonPath("$[0].tipo").value("LINKEDIN"));
    }

    @Test
    @WithMockUser(username = "admin")
    void buscarRedeSocial_autenticado_retornaObjeto() throws Exception {
        Usuario user = usuario(1L, "admin", perfil("ADMIN"));
        when(usuarioService.buscarPorLogin("admin")).thenReturn(user);
        when(usuarioRedeSocialService.buscarPorIdEUsuario(10L, 1L)).thenReturn(
            UsuarioRedeSocialDTO.builder().id(10L).tipo(TipoRedeSocial.GITHUB).url("https://github.com").ativo(true).build()
        );

        mockMvc.perform(get("/minha-conta/redes-sociais/10").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.tipo").value("GITHUB"));
    }

    @Test
    @WithMockUser(username = "admin")
    void inserirRedeSocial_sucesso() throws Exception {
        Usuario user = usuario(1L, "admin", perfil("ADMIN"));
        when(usuarioService.buscarPorLogin("admin")).thenReturn(user);
        when(usuarioRedeSocialService.inserir(any(), eq(1L), eq("admin"))).thenReturn(
            UsuarioRedeSocialDTO.builder().id(10L).tipo(TipoRedeSocial.LINKEDIN).url("https://linkedin.com/in/teste").ativo(true).build()
        );

        mockMvc.perform(post("/minha-conta/redes-sociais").with(csrf())
                .param("tipo", "LINKEDIN")
                .param("url", "https://linkedin.com/in/teste")
                .param("ativo", "true")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.redeSocial.id").value(10L));
    }

    @Test
    @WithMockUser(username = "admin")
    void editarRedeSocial_sucesso() throws Exception {
        Usuario user = usuario(1L, "admin", perfil("ADMIN"));
        when(usuarioService.buscarPorLogin("admin")).thenReturn(user);
        when(usuarioRedeSocialService.editar(eq(10L), any(), eq(1L), eq("admin"))).thenReturn(
            UsuarioRedeSocialDTO.builder().id(10L).tipo(TipoRedeSocial.LINKEDIN).url("https://linkedin.com/in/novo").ativo(true).build()
        );

        mockMvc.perform(put("/minha-conta/redes-sociais/10").with(csrf())
                .param("tipo", "LINKEDIN")
                .param("url", "https://linkedin.com/in/novo")
                .param("ativo", "true")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.redeSocial.url").value("https://linkedin.com/in/novo"));
    }

    @Test
    @WithMockUser(username = "admin")
    void excluirRedeSocial_sucesso() throws Exception {
        Usuario user = usuario(1L, "admin", perfil("ADMIN"));
        when(usuarioService.buscarPorLogin("admin")).thenReturn(user);

        mockMvc.perform(delete("/minha-conta/redes-sociais/10").with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }
}

