package br.com.diegocordeiro.dscproject.web.sistema.controller.parametro;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroGlobalEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroGlobalListaDTO;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.parametro.ParametroGlobalService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParametroController.class)
@Import(SecurityConfig.class)
class ParametroControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private ParametroGlobalService parametroGlobalService;
    @MockitoBean
    private AutorizacaoService autorizacaoService;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setup(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static ParametroGlobal parametro(Long id, TipoParametro tipo, String valor) {
        ParametroGlobal p = new ParametroGlobal();
        p.setId(id);
        p.setCodigo("CONTA_MOEDA_PADRAO");
        p.setNome("Moeda padrão da conta");
        p.setDescricao("A moeda padrão de novas contas.");
        p.setModulo("Contas");
        p.setTipoDado(tipo);
        p.setValor(valor);
        p.setValorDefault("BRL");
        return p;
    }

    // ---------- RN01 / BDD 16.0, 16.1 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void listarDados_comPermissaoListar_200ComCamposEsperados() throws Exception {
        when(parametroGlobalService.listarParaGrid())
            .thenReturn(List.of(new ParametroGlobalListaDTO(parametro(7L, TipoParametro.STRING, "BRL"))));

        mockMvc.perform(get("/parametros/listar-dados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("CONTA_MOEDA_PADRAO"))
            .andExpect(jsonPath("$[0].tipoDado").value("STRING"))
            .andExpect(jsonPath("$[0].valor").value("BRL"))
            .andExpect(jsonPath("$[0].orfa").value(false));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listarDados_perfilUser_403() throws Exception {
        mockMvc.perform(get("/parametros/listar-dados")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void listar_renderizaPaginaComFragmentsColunaAcoesEJs() throws Exception {
        mockMvc.perform(get("/parametros/listar"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("id=\"modalFiltro\""),
                org.hamcrest.Matchers.containsString("id=\"modalConfirmar\""),
                org.hamcrest.Matchers.containsString("id=\"modalHistorico\""),
                org.hamcrest.Matchers.containsString(">Ações<"),
                org.hamcrest.Matchers.containsString("<ol class=\"breadcrumb\">"),
                org.hamcrest.Matchers.containsString("/js/parametro/listar.js"))));
    }

    // ---------- EDP03 / RN01 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_EDITAR")
    void buscar_comPermissaoEditar_devolveEstadoAtual() throws Exception {
        when(parametroGlobalService.buscarParaEdicao(7L))
            .thenReturn(new ParametroGlobalEdicaoDTO(parametro(7L, TipoParametro.STRING, "BRL")));

        mockMvc.perform(get("/parametros/buscar/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valor").value("BRL"))
            .andExpect(jsonPath("$.tipoDado").value("STRING"));
    }

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void buscar_soComListar_403() throws Exception {
        mockMvc.perform(get("/parametros/buscar/7")).andExpect(status().isForbidden());
    }

    // ---------- EDP04 / BDD 16.2 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_EDITAR")
    void editar_valorEMotivo_200ComMsg01() throws Exception {
        mockMvc.perform(put("/parametros/editar/7").with(csrf())
                .param("valor", "USD").param("motivo", "Conta internacional")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.mensagem").value("Parâmetro atualizado com sucesso."));

        verify(parametroGlobalService).editarValor(eq(7L), any());
    }

    // ---------- RN03 / BDD 16.7 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_EDITAR")
    void editar_semMotivo_422ComErroDeCampoNoMotivo() throws Exception {
        mockMvc.perform(put("/parametros/editar/7").with(csrf())
                .param("valor", "USD").param("motivo", "")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errosCampos.motivo").value("O motivo é obrigatório."));
    }

    // ---------- RN02 no serviço / BDD 16.3 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_EDITAR")
    void editar_valorInvalidoParaTipo_422ComMensagem() throws Exception {
        doThrow(new RegraNegocioException("valor", "parametro.valor.inteiro.invalido"))
            .when(parametroGlobalService).editarValor(eq(7L), any());

        mockMvc.perform(put("/parametros/editar/7").with(csrf())
                .param("valor", "abc").param("motivo", "motivo")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem")
                .value("O valor informado não é um número inteiro válido."));
    }

    // ---------- EDP06 / RN07 / BDD 16.13 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_EDITAR")
    void restaurarPadrao_comMotivo_200ComMsg10() throws Exception {
        mockMvc.perform(put("/parametros/restaurar-padrao/7").with(csrf())
                .param("motivo", "voltando ao padrão")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem").value("Valor restaurado para o padrão do sistema."));

        verify(parametroGlobalService).restaurarPadrao(eq(7L), eq("voltando ao padrão"));
    }

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void restaurarPadrao_soComListar_403() throws Exception {
        mockMvc.perform(put("/parametros/restaurar-padrao/7").with(csrf())
                .param("motivo", "x"))
            .andExpect(status().isForbidden());
    }

    // ---------- EDP05 ----------

    @Test
    @WithMockUser(authorities = "PERM_PARAMETROS_LISTAR")
    void historico_comPermissaoListar_200() throws Exception {
        when(parametroGlobalService.buscarHistorico(eq(7L), any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/parametros/historico/7"))
            .andExpect(status().isOk());
    }

    // ---------- BDD 16.10 — não há criar nem excluir ----------

    @Test
    @WithMockUser(authorities = {"PERM_PARAMETROS_LISTAR", "PERM_PARAMETROS_EDITAR"})
    void naoExisteEndpointDeInserirNemDeExcluir() throws Exception {
        mockMvc.perform(post("/parametros/inserir").with(csrf()))
            .andExpect(status().is4xxClientError());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/parametros/excluir/7").with(csrf()))
            .andExpect(status().is4xxClientError());
    }
}
