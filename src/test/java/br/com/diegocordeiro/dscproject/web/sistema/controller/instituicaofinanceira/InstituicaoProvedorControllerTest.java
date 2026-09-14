package br.com.diegocordeiro.dscproject.web.sistema.controller.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.ProvedorOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.instituicaofinanceira.InstituicaoProvedorService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstituicaoProvedorController.class)
@Import(SecurityConfig.class)
class InstituicaoProvedorControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private InstituicaoProvedorService instituicaoProvedorService;

    @MockitoBean
    private OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository;

    @MockitoBean
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @MockitoBean
    private OpfiProvedorRepository opfiProvedorRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("Listar view com autoridade PERM_INSTITUICOES_PROVEDOR_LISTAR")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_LISTAR")
    void listar_comPermissao_deveRetornar200EViewListar() throws Exception {
        when(instituicaoProvedorService.listarProvedoresOpcoes()).thenReturn(List.of(
                new ProvedorOpcaoDTO(1L, "PLUGGY", "Pluggy", true)
        ));

        mockMvc.perform(get("/instituicoes-provedor/listar"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/instituicao-provedor/listar"))
                .andExpect(model().attributeExists("provedores"));
    }

    @Test
    @DisplayName("Bloquear acesso sem permissão de listagem")
    @WithMockUser(authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/instituicoes-provedor/listar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Listar dados JSON com permissão")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_LISTAR")
    void listarDados_comPermissao_deveRetornarJsonGrid() throws Exception {
        when(instituicaoProvedorService.listarParaGrid()).thenReturn(List.of(
                InstituicaoProvedorGridDTO.builder()
                        .id(1L)
                        .provedorNome("Pluggy")
                        .instituicaoNome("Nubank")
                        .idExterno("pluggy_nubank")
                        .build()
        ));

        mockMvc.perform(get("/instituicoes-provedor/listar-dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].provedorNome").value("Pluggy"))
                .andExpect(jsonPath("$[0].instituicaoNome").value("Nubank"));
    }

    @Test
    @DisplayName("Buscar dados para edição")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_EDITAR")
    void buscar_comPermissao_deveRetornarEdicaoDTO() throws Exception {
        InstituicaoProvedorEdicaoDTO dto = new InstituicaoProvedorEdicaoDTO();
        dto.setId(1L);
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("pluggy_nubank");

        when(instituicaoProvedorService.buscarParaEdicao(1L)).thenReturn(dto);

        mockMvc.perform(get("/instituicoes-provedor/buscar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idExterno").value("pluggy_nubank"));
    }

    @Test
    @DisplayName("Inserir vínculo com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_INSERIR")
    void inserir_comDadosValidos_deveRetornarOk() throws Exception {
        when(opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inst));

        OpfiProvedor prov = new OpfiProvedor();
        prov.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(prov));

        when(instituicaoProvedorService.inserir(any())).thenReturn(new OpfiInstituicaoProvedor());

        mockMvc.perform(post("/instituicoes-provedor/inserir")
                        .with(csrf())
                        .param("provedorId", "1")
                        .param("instituicaoId", "10")
                        .param("idExterno", "pluggy_nubank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoProvedorService).inserir(any());
    }

    @Test
    @DisplayName("Inserir vínculo com par duplicado retorna 422")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_INSERIR")
    void inserir_quandoParDuplicado_deveRetornar422() throws Exception {
        when(opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(1L);
        when(opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inst));

        OpfiProvedor prov = new OpfiProvedor();
        prov.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(prov));

        mockMvc.perform(post("/instituicoes-provedor/inserir")
                        .with(csrf())
                        .param("provedorId", "1")
                        .param("instituicaoId", "10")
                        .param("idExterno", "pluggy_nubank"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosNegocio.instituicaoId").exists());

        verify(instituicaoProvedorService, never()).inserir(any());
    }

    @Test
    @DisplayName("Editar vínculo com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_EDITAR")
    void editar_comDadosValidos_deveRetornarOk() throws Exception {
        when(opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, 2L)).thenReturn(0L);
        when(opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank_v2", 2L)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inst));

        OpfiProvedor prov = new OpfiProvedor();
        prov.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(prov));

        mockMvc.perform(put("/instituicoes-provedor/editar/2")
                        .with(csrf())
                        .param("provedorId", "1")
                        .param("instituicaoId", "10")
                        .param("idExterno", "pluggy_nubank_v2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoProvedorService).editar(eq(2L), any());
    }

    @Test
    @DisplayName("Excluir vínculo com sucesso")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_EXCLUIR", username = "admin_user")
    void excluir_comPermissao_deveRetornarOk() throws Exception {
        mockMvc.perform(delete("/instituicoes-provedor/excluir/5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(instituicaoProvedorService).excluir(eq(5L), eq("admin_user"));
    }

    @Test
    @DisplayName("Listar opções de provedores")
    @WithMockUser(authorities = "PERM_INSTITUICOES_PROVEDOR_LISTAR")
    void listarProvedoresOpcoes_comPermissao_deveRetornarListaJson() throws Exception {
        when(instituicaoProvedorService.listarProvedoresOpcoes()).thenReturn(List.of(
                new ProvedorOpcaoDTO(1L, "PLUGGY", "Pluggy", true)
        ));

        mockMvc.perform(get("/instituicoes-provedor/provedores-opcoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pluggy"));
    }
}
