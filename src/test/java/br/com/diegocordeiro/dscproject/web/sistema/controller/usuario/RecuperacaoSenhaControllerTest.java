package br.com.diegocordeiro.dscproject.web.sistema.controller.usuario;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.usuario.RecuperacaoSenhaService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioRedeSocialService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
import br.com.diegocordeiro.dscproject.service.exceptions.TokenRecuperacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class RecuperacaoSenhaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private RecuperacaoSenhaService recuperacaoSenhaService;
    @MockitoBean
    private UsuarioService usuarioService;
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

    // ---------- BDD 16.10 — não revela existência de conta ----------

    @Test
    void solicitar_publico_sempreRespondeMSG15() throws Exception {
        doNothing().when(recuperacaoSenhaService).solicitar(any());

        mockMvc.perform(post("/usuarios/recuperar-senha/solicitar").with(csrf())
                .param("email", "existe@test.com")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem")
                .value("Se houver uma conta com esse e-mail, enviamos um link para redefinir a senha."));

        mockMvc.perform(post("/usuarios/recuperar-senha/solicitar").with(csrf())
                .param("email", "naoexiste@test.com")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem")
                .value("Se houver uma conta com esse e-mail, enviamos um link para redefinir a senha."));
    }

    @Test
    void solicitar_emailInvalido_422ErroDeCampo() throws Exception {
        mockMvc.perform(post("/usuarios/recuperar-senha/solicitar").with(csrf())
                .param("email", "nao-e-email")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosCampos.email").exists());
    }

    // ---------- BDD 16.11 — token expirado ----------

    @Test
    void confirmar_tokenExpirado_422ComMSG18() throws Exception {
        doThrow(TokenRecuperacaoException.expirado())
            .when(recuperacaoSenhaService).confirmar(any(), any());

        mockMvc.perform(post("/usuarios/recuperar-senha/confirmar").with(csrf())
                .param("token", "abc")
                .param("senha", "novaSenha")
                .param("confirmacaoSenha", "novaSenha")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Link de redefinição expirado. Solicite um novo."));
    }

    @Test
    void confirmar_senhasDiferentes_422ComMSG07() throws Exception {
        mockMvc.perform(post("/usuarios/recuperar-senha/confirmar").with(csrf())
                .param("token", "abc")
                .param("senha", "novaSenha")
                .param("confirmacaoSenha", "outra")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosNegocio.confirmacaoSenha").value("As senhas não conferem."));
    }

    @Test
    void confirmar_dadosValidos_200ComMSG14() throws Exception {
        doNothing().when(recuperacaoSenhaService).confirmar(any(), any());

        mockMvc.perform(post("/usuarios/recuperar-senha/confirmar").with(csrf())
                .param("token", "abc")
                .param("senha", "novaSenha")
                .param("confirmacaoSenha", "novaSenha")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem")
                .value("Senha redefinida com sucesso. Você já pode entrar com a nova senha."));
    }
}
