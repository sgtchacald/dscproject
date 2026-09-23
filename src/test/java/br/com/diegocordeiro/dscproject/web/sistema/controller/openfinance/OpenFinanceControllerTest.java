package br.com.diegocordeiro.dscproject.web.sistema.controller.openfinance;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceConexaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialExibicaoDTO;
import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceContaExterna;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceConexaoService;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceCredencialService;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceSincronizacaoService;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OpenFinanceController.class)
@Import(SecurityConfig.class)
class OpenFinanceControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private OpenFinanceConexaoService conexaoService;

    @MockitoBean
    private OpenFinanceCredencialService credencialService;

    @MockitoBean
    private OpenFinanceSincronizacaoService sincronizacaoService;

    @MockitoBean
    private OpenFinanceProvedorRepository provedorRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario usuarioMock;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setLogin("diego");
        when(usuarioRepository.findByLogin("diego")).thenReturn(Optional.of(usuarioMock));
    }

    @Test
    @DisplayName("GET /open-finance/conexoes sem permissão retorna 403")
    @WithMockUser(username = "diego", authorities = "ROLE_USER")
    void listar_semPermissao_retorna403() throws Exception {
        mockMvc.perform(get("/open-finance/conexoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /open-finance/conexoes com PERM_OPEN_FINANCE_LISTAR retorna view listar")
    @WithMockUser(username = "diego", authorities = "PERM_OPEN_FINANCE_LISTAR")
    void listar_comPermissao_retornaView() throws Exception {
        when(provedorRepository.findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc()).thenReturn(List.of());

        mockMvc.perform(get("/open-finance/conexoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/open-finance/listar"));
    }

    @Test
    @DisplayName("GET /open-finance/dados com PERM_OPEN_FINANCE_LISTAR retorna grid em JSON")
    @WithMockUser(username = "diego", authorities = "PERM_OPEN_FINANCE_LISTAR")
    void listarDados_comPermissao_retornaJson() throws Exception {
        OpenFinanceConexaoGridDTO dto = OpenFinanceConexaoGridDTO.builder()
                .id(10L)
                .idExterno("item-123")
                .status(StatusConexao.ATUALIZADO)
                .statusDescricao("Atualizado")
                .instituicaoNome("Nubank")
                .build();

        when(conexaoService.listarConexoes(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/open-finance/dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].instituicaoNome").value("Nubank"));
    }

    @Test
    @DisplayName("POST /open-finance/credenciais com PERM_OPEN_FINANCE_CONFIGURAR_CREDENCIAL salva credencial")
    @WithMockUser(username = "diego", authorities = "PERM_OPEN_FINANCE_CONFIGURAR_CREDENCIAL")
    void salvarCredencial_comPermissao_salvaERetornaOk() throws Exception {
        OpenFinanceCredencialDTO dto = new OpenFinanceCredencialDTO();
        dto.setProvedorId(1L);
        dto.setClientId("client-abc");
        dto.setClientSecret("secret-xyz");
        dto.setAmbiente(AmbienteCredencial.PRODUCTION);

        mockMvc.perform(post("/open-finance/credenciais").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(credencialService).salvarCredencial(eq(1L), any(OpenFinanceCredencialDTO.class));
    }

    @Test
    @DisplayName("POST /open-finance/connect-token gera token para o widget")
    @WithMockUser(username = "diego", authorities = "PERM_OPEN_FINANCE_CONECTAR")
    void gerarConnectToken_comPermissao_retornaToken() throws Exception {
        when(conexaoService.gerarConnectToken(1L, null)).thenReturn("token-widget-jwt-123");

        mockMvc.perform(post("/open-finance/connect-token").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectToken").value("token-widget-jwt-123"));
    }

    @Test
    @DisplayName("POST /open-finance/conexoes/callback cria conexão e retorna 201 Created")
    @WithMockUser(username = "diego", authorities = "PERM_OPEN_FINANCE_CONECTAR")
    void callback_comPermissao_retorna201() throws Exception {
        OpenFinanceConexao c = new OpenFinanceConexao();
        c.setId(50L);
        when(conexaoService.processarCallback(1L, "item-pluggy-novo")).thenReturn(c);

        mockMvc.perform(post("/open-finance/conexoes/callback").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\": \"item-pluggy-novo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("DELETE /open-finance/conexoes/{id} desconecta conexão")
    @WithMockUser(username = "diego", authorities = "PERM_OPEN_FINANCE_DESCONECTAR")
    void desconectar_comPermissao_retornaOk() throws Exception {
        mockMvc.perform(delete("/open-finance/conexoes/50").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(conexaoService).desconectar(1L, 50L);
    }
}
