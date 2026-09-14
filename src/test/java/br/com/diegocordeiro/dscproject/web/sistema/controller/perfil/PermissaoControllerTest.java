package br.com.diegocordeiro.dscproject.web.sistema.controller.perfil;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.permissao.PermissaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.dto.catalogo.SincronizacaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.perfil.PermissaoCatalogoService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissaoController.class)
@Import(SecurityConfig.class)
class PermissaoControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private PermissaoCatalogoService permissaoCatalogoService;
    @MockitoBean
    private AutorizacaoService autorizacaoService;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setup(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ---------- EDP04 / RN01 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_LISTAR")
    void listarDados_comPermissaoListar_200() throws Exception {
        Permissao p = new Permissao("USUARIOS_LISTAR", "Listar usuários", null, "Usuários");
        when(permissaoCatalogoService.listar()).thenReturn(List.of(new PermissaoCatalogoDTO(p)));

        mockMvc.perform(get("/permissoes/listar-dados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("USUARIOS_LISTAR"))
            .andExpect(jsonPath("$[0].modulo").value("Usuários"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listarDados_semPermissao_403() throws Exception {
        mockMvc.perform(get("/permissoes/listar-dados")).andExpect(status().isForbidden());
    }

    // ---------- EDP08 / RN01 / BDD 16.8 ----------

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_SINCRONIZAR_CATALOGO")
    void sincronizar_comPermissao_200ComMsg11() throws Exception {
        when(permissaoCatalogoService.sincronizar()).thenReturn(new SincronizacaoCatalogoDTO(1, 2));

        mockMvc.perform(post("/permissoes/sincronizar-catalogo").with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.mensagem")
                .value("Catálogo sincronizado: 1 permissões novas, 2 marcadas como órfãs."));
    }

    @Test
    @WithMockUser(authorities = "PERM_PERFIS_EDITAR")
    void sincronizar_semPermissaoSincronizar_403() throws Exception {
        mockMvc.perform(post("/permissoes/sincronizar-catalogo").with(csrf()))
            .andExpect(status().isForbidden());
    }
}
