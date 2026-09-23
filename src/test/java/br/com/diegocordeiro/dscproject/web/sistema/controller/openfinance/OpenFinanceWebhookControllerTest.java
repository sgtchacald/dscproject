package br.com.diegocordeiro.dscproject.web.sistema.controller.openfinance;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyWebhookPayloadDTO;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceWebhookService;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpenFinanceWebhookController.class)
@Import(SecurityConfig.class)
class OpenFinanceWebhookControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private OpenFinanceWebhookService webhookService;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("POST /api/open-finance/webhook/{provedorCodigo} público sem autenticação e sem CSRF responde 200")
    void receberWebhook_publicoSemCsrf_retornaOk() throws Exception {
        PluggyWebhookPayloadDTO payload = new PluggyWebhookPayloadDTO();
        payload.setId("evt-123");
        payload.setEvent("item/updated");
        payload.setItemId("item-456");

        mockMvc.perform(post("/api/open-finance/webhook/PLUGGY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(webhookService).receberEvento(eq("PLUGGY"), any(PluggyWebhookPayloadDTO.class), any());
    }
}
