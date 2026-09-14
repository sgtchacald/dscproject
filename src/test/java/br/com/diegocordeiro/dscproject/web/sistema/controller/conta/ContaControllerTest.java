package br.com.diegocordeiro.dscproject.web.sistema.controller.conta;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.conta.ContaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaGridDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.conta.ContaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContaController.class)
@Import(SecurityConfig.class)
class ContaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private ContaService contaService;

    @MockitoBean
    private ContaRepository contaRepository;

    @MockitoBean
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AutorizacaoService autorizacaoService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setLogin("user_teste");
        usuarioMock.setNome("Usuário Teste");

        when(usuarioRepository.findByLogin("user_teste")).thenReturn(Optional.of(usuarioMock));
    }

    @Test
    @DisplayName("Listar view com autoridade PERM_CONTAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_LISTAR")
    void listar_comPermissao_deveRetornar200EViewListar() throws Exception {
        mockMvc.perform(get("/contas/listar"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/conta/listar"))
                .andExpect(model().attributeExists("tipos"))
                .andExpect(model().attributeExists("moedas"));
    }

    @Test
    @DisplayName("Botão Cancelar dos modais de conta e de ajuste de saldo é vermelho (btn-danger)")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_LISTAR")
    void listar_botoesCancelarDosModais_devemSerBtnDanger() throws Exception {
        String html = mockMvc.perform(get("/contas/listar"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long qtdCancelarVermelho = java.util.regex.Pattern.compile("class=\"btn btn-danger\" data-bs-dismiss=\"modal\"")
                .matcher(html).results().count();

        org.junit.jupiter.api.Assertions.assertEquals(2, qtdCancelarVermelho);
    }

    @Test
    @DisplayName("SB01 - O combobox de tipo de conta vem do enum TipoConta, não de <option> fixa no HTML")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_LISTAR")
    void listar_deveRenderizarOsQuatroTiposDeContaDoEnum() throws Exception {
        mockMvc.perform(get("/contas/listar"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("value=\"CORRENTE\""),
                        org.hamcrest.Matchers.containsString("value=\"POUPANCA\""),
                        org.hamcrest.Matchers.containsString("value=\"INVESTIMENTO\""),
                        org.hamcrest.Matchers.containsString("value=\"CARTEIRA\""))));
    }

    @Test
    @DisplayName("Bloquear acesso de usuário sem permissão à listagem de contas")
    @WithMockUser(username = "user_teste", authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/contas/listar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Listar dados JSON da grid com isolamento por usuário")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_LISTAR")
    void listarDados_comPermissao_deveRetornarJsonGrid() throws Exception {
        when(contaService.listarParaGrid(1L)).thenReturn(List.of(
                ContaGridDTO.builder()
                        .id(10L)
                        .descricao("Conta Corrente Principal")
                        .instituicaoNome("Banco do Brasil")
                        .tipo(TipoConta.CORRENTE)
                        .saldo(new BigDecimal("1250.00"))
                        .moeda("BRL")
                        .ativo(true)
                        .consideraSaldo(true)
                        .build()
        ));

        mockMvc.perform(get("/contas/listar-dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].descricao").value("Conta Corrente Principal"))
                .andExpect(jsonPath("$[0].saldo").value(1250.00));

        verify(contaService).listarParaGrid(1L);
    }

    @Test
    @DisplayName("Buscar dados para edição de conta pertencente ao usuário")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_EDITAR")
    void buscar_quandoContaDoUsuario_deveRetornarEdicaoDTO() throws Exception {
        ContaEdicaoDTO dto = new ContaEdicaoDTO();
        dto.setId(10L);
        dto.setDescricao("Conta Corrente Principal");
        dto.setInstituicaoId(5L);
        dto.setTipo(TipoConta.CORRENTE);
        dto.setSaldo(new BigDecimal("1250.00"));
        dto.setAtivo(true);

        when(contaService.buscarParaEdicao(10L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/contas/buscar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.descricao").value("Conta Corrente Principal"));
    }

    @Test
    @DisplayName("Buscar conta pertencente a outro usuário retorna 404 (RN02 / BDD 16.3 / 16.4)")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_EDITAR")
    void buscar_quandoContaDeOutroUsuario_deveRetornar404() throws Exception {
        when(contaService.buscarParaEdicao(99L, 1L))
                .thenThrow(new RegistroNaoEncontradoException("msg.conta.nao-encontrada"));

        mockMvc.perform(get("/contas/buscar/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Inserir conta com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_INSERIR")
    void inserir_comDadosValidos_deveRetornarOk() throws Exception {
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta XP", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(5L)).thenReturn(Optional.of(inst));

        when(contaService.inserir(any(), eq(1L), eq("user_teste"))).thenReturn(new Conta());

        mockMvc.perform(post("/contas/inserir")
                        .with(csrf())
                        .param("descricao", "Conta XP")
                        .param("instituicaoId", "5")
                        .param("tipo", "INVESTIMENTO")
                        .param("saldoInicial", "5000.00")
                        .param("consideraSaldo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(contaService).inserir(any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("RF03 / RT10 / SB03 - Moeda escolhida no formulário chega ao Service")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_INSERIR")
    void inserir_comMoedaEscolhida_deveRepassarAoService() throws Exception {
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta em Dólar", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(5L)).thenReturn(Optional.of(inst));

        when(contaService.inserir(any(), eq(1L), eq("user_teste"))).thenReturn(new Conta());

        mockMvc.perform(post("/contas/inserir")
                        .with(csrf())
                        .param("descricao", "Conta em Dólar")
                        .param("instituicaoId", "5")
                        .param("tipo", "INVESTIMENTO")
                        .param("moeda", "USD")
                        .param("saldoInicial", "5000.00")
                        .param("consideraSaldo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        var captor = org.mockito.ArgumentCaptor.forClass(br.com.diegocordeiro.dscproject.dto.conta.ContaFormDTO.class);
        verify(contaService).inserir(captor.capture(), eq(1L), eq("user_teste"));
        org.junit.jupiter.api.Assertions.assertEquals("USD", captor.getValue().getMoeda());
    }

    @Test
    @DisplayName("Inserir conta com descrição duplicada retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_INSERIR")
    void inserir_quandoDescricaoDuplicada_deveRetornar422() throws Exception {
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta XP", null)).thenReturn(1L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findById(5L)).thenReturn(Optional.of(inst));

        mockMvc.perform(post("/contas/inserir")
                        .with(csrf())
                        .param("descricao", "Conta XP")
                        .param("instituicaoId", "5")
                        .param("tipo", "INVESTIMENTO"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosNegocio.descricao").exists());

        verify(contaService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("Editar conta com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_EDITAR")
    void editar_comDadosValidos_deveRetornarOk() throws Exception {
        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(5L);

        Conta contaOriginal = new Conta();
        contaOriginal.setId(10L);
        contaOriginal.setInstituicao(inst);

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaOriginal));
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta XP Atualizada", 10L)).thenReturn(0L);

        mockMvc.perform(put("/contas/editar/10")
                        .with(csrf())
                        .param("descricao", "Conta XP Atualizada")
                        .param("instituicaoId", "5")
                        .param("tipo", "INVESTIMENTO")
                        .param("consideraSaldo", "true")
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(contaService).editar(eq(10L), any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("Ajustar saldo com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_AJUSTAR_SALDO")
    void ajustarSaldo_comDadosValidos_deveRetornarOk() throws Exception {
        mockMvc.perform(put("/contas/ajustar-saldo/10")
                        .with(csrf())
                        .param("novoSaldo", "3500.50")
                        .param("observacao", "Conferência de extrato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(contaService).ajustarSaldo(eq(10L), any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("Desativar conta com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_DESATIVAR")
    void desativar_comPermissao_deveRetornarOk() throws Exception {
        mockMvc.perform(put("/contas/desativar/10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(contaService).desativar(10L, 1L);
    }

    @Test
    @DisplayName("Excluir conta com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_EXCLUIR")
    void excluir_comPermissao_deveRetornarOk() throws Exception {
        mockMvc.perform(delete("/contas/excluir/10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(contaService).excluir(10L, 1L, "user_teste");
    }

    @Test
    @DisplayName("Listar opções em combobox para usuário autenticado")
    @WithMockUser(username = "user_teste", authorities = "PERM_CONTAS_LISTAR")
    void listarOpcoes_usuarioAutenticado_deveRetornarListaJson() throws Exception {
        when(contaService.listarOpcoesCombobox(1L)).thenReturn(List.of(
                new ContaOpcaoDTO(10L, "Conta XP", TipoConta.INVESTIMENTO, "BRL", "XP Investimentos", new BigDecimal("1000.00"))
        ));

        mockMvc.perform(get("/contas/opcoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Conta XP"))
                .andExpect(jsonPath("$[0].tipo").value("INVESTIMENTO"));

        verify(contaService).listarOpcoesCombobox(1L);
    }
}
