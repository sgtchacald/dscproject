package br.com.diegocordeiro.dscproject.web.sistema.controller.fatura;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCartaoGridDTO;
import br.com.diegocordeiro.dscproject.enums.StatusFatura;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.cartao.CartaoCreditoService;
import br.com.diegocordeiro.dscproject.service.conta.ContaService;
import br.com.diegocordeiro.dscproject.service.fatura.FaturaCartaoService;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FaturaCartaoController.class)
@Import(SecurityConfig.class)
class FaturaCartaoControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private FaturaCartaoService faturaCartaoService;

    @MockitoBean
    private CartaoCreditoService cartaoCreditoService;

    @MockitoBean
    private ContaService contaService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setLogin("user_teste");
        usuarioMock.setNome("Usuário Teste");

        when(usuarioRepository.findByLogin("user_teste")).thenReturn(Optional.of(usuarioMock));
    }

    @Test
    @DisplayName("Deve carregar tela de faturas quando usuario tem autoridade FATURAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_FATURAS_LISTAR")
    void deveCarregarTelaListarComPermissao() throws Exception {
        when(cartaoCreditoService.listarOpcoesCombobox(1L)).thenReturn(List.of());
        when(contaService.listarOpcoesCombobox(1L)).thenReturn(List.of());

        mockMvc.perform(get("/faturas-cartao/listar"))
            .andExpect(status().isOk())
            .andExpect(view().name("sistema/modulos/fatura-cartao/listar"));
    }

    @Test
    @DisplayName("Deve retornar 403 ao tentar listar faturas sem permissao")
    @WithMockUser(username = "user_teste", authorities = "OUTRA_PERMISSAO")
    void deveNegarAcessoSemPermissao() throws Exception {
        mockMvc.perform(get("/faturas-cartao/listar"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar dados das faturas em JSON com autoridade FATURAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_FATURAS_LISTAR")
    void deveRetornarDadosFaturasEmJson() throws Exception {
        FaturaCartaoGridDTO dto = FaturaCartaoGridDTO.builder()
            .id(100L)
            .cartaoId(10L)
            .cartaoDescricao("Nubank")
            .competencia(YearMonth.of(2026, 10))
            .dataVencimento(LocalDate.of(2026, 10, 10))
            .valorTotal(new BigDecimal("350.00"))
            .status(StatusFatura.ABERTA)
            .build();

        when(faturaCartaoService.listarFaturas(10L, 1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/faturas-cartao/dados?cartaoId=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(100))
            .andExpect(jsonPath("$[0].cartaoDescricao").value("Nubank"))
            .andExpect(jsonPath("$[0].status").value("ABERTA"));
    }

    @Test
    @DisplayName("Deve fechar fatura com autoridade FATURAS_FECHAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_FATURAS_FECHAR")
    void deveFecharFaturaComPermissao() throws Exception {
        mockMvc.perform(post("/faturas-cartao/100/fechar")
                .param("dataFechamento", "2026-10-03")
                .param("dataVencimento", "2026-10-10")
                .param("valorEncargos", "15.00")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("Deve reabrir fatura com autoridade FATURAS_REABRIR")
    @WithMockUser(username = "user_teste", authorities = "PERM_FATURAS_REABRIR")
    void deveReabrirFaturaComPermissao() throws Exception {
        mockMvc.perform(post("/faturas-cartao/100/reabrir").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }
}
