package br.com.diegocordeiro.dscproject.config;

import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class RememberMeIntegrationTest {

    private static final String LOGIN = "remember-me-teste";
    private static final String EMAIL = "remember-me-teste@teste.local";
    private static final String SENHA = "SenhaDeTeste-123";

    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        removerUsuarioDeTeste();

        Perfil user = perfilRepository.findByCodigo("USER").orElseThrow();
        Usuario u = new Usuario();
        u.setNome("Remember Me");
        u.setGenero(Genero.OUTRO);
        u.setLogin(LOGIN);
        u.setEmail(EMAIL);
        u.setSenha(passwordEncoder.encode(SENHA));
        u.setPerfil(user);
        usuarioRepository.save(u);
    }

    @AfterEach
    void limpar() {
        removerUsuarioDeTeste();
    }

    private void removerUsuarioDeTeste() {
        Usuario existente = usuarioRepository.findByLoginOrEmail(LOGIN, EMAIL);
        if (existente != null) {
            usuarioRepository.delete(existente);
        }
    }

    @Test
    void loginComLembrar_emiteCookieQueAutenticaEmSessaoNova() throws Exception {
        MvcResult login = mockMvc.perform(post("/login").with(csrf())
                .param("username", LOGIN)
                .param("password", SENHA)
                .param("lembrar", "true"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        Cookie rememberMe = login.getResponse().getCookie("remember-me");
        assertThat(rememberMe).as("cookie remember-me").isNotNull();
        assertThat(rememberMe.getMaxAge()).isPositive();

        // Só o cookie remember-me, sem sessão: rota protegida abre em vez de
        // redirecionar para /login.
        mockMvc.perform(get("/").cookie(rememberMe))
            .andExpect(status().isOk());
    }

    @Test
    void loginSemLembrar_naoEmiteCookieRememberMe() throws Exception {
        MvcResult login = mockMvc.perform(post("/login").with(csrf())
                .param("username", LOGIN)
                .param("password", SENHA))
            .andReturn();

        assertThat(login.getResponse().getCookie("remember-me")).isNull();
    }
}
