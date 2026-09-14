package br.com.diegocordeiro.dscproject.web.sistema.controller.cartao;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoGridDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.cartao.CartaoCreditoService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
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
import static org.mockito.Mockito.never;
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

@WebMvcTest(CartaoCreditoController.class)
@Import(SecurityConfig.class)
class CartaoCreditoControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private CartaoCreditoService cartaoCreditoService;

    @MockitoBean
    private CartaoCreditoRepository cartaoCreditoRepository;

    @MockitoBean
    private ContaRepository contaRepository;

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
    @DisplayName("Listar view com autoridade PERM_CARTOES_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_LISTAR")
    void listar_comPermissao_deveRetornar200EViewListar() throws Exception {
        mockMvc.perform(get("/cartoes/listar"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/cartao/listar"));
    }

    @Test
    @DisplayName("SB01 - O combobox de bandeira vem do enum BandeiraCartao, não de <option> fixa no HTML")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_LISTAR")
    void listar_deveRenderizarAsQuatroBandeirasDoEnum() throws Exception {
        mockMvc.perform(get("/cartoes/listar"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("value=\"VISA\""),
                        org.hamcrest.Matchers.containsString("value=\"MASTERCARD\""),
                        org.hamcrest.Matchers.containsString("value=\"ELO\""),
                        org.hamcrest.Matchers.containsString("value=\"AMEX\""))));
    }

    @Test
    @DisplayName("Botão Cancelar do modal de cartão é vermelho (btn-danger)")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_LISTAR")
    void listar_botaoCancelarDoModal_deveSerBtnDanger() throws Exception {
        String html = mockMvc.perform(get("/cartoes/listar"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long qtdCancelarVermelho = java.util.regex.Pattern.compile("class=\"btn btn-danger\" data-bs-dismiss=\"modal\"")
                .matcher(html).results().count();

        org.junit.jupiter.api.Assertions.assertEquals(1, qtdCancelarVermelho);
    }

    @Test
    @DisplayName("BDD 16.1 / RN01 - Bloquear acesso de perfil sem CARTOES_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/cartoes/listar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("BDD 16.1 - Bloquear acesso de perfil sem CARTOES_LISTAR também no endpoint de dados")
    @WithMockUser(username = "user_teste", authorities = "ROLE_USER")
    void listarDados_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/cartoes/listar-dados"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("BDD 16.0 / BDD 16.2 - Listar dados JSON da grid com isolamento por usuário")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_LISTAR")
    void listarDados_comPermissao_deveRetornarGridDoUsuario() throws Exception {
        when(cartaoCreditoService.listarParaGrid(1L)).thenReturn(List.of(
                CartaoCreditoGridDTO.builder()
                        .id(10L)
                        .descricao("Nubank Ultravioleta")
                        .bandeira("MASTERCARD")
                        .finalCartao("1234")
                        .limite(new BigDecimal("5000.00"))
                        .diaFechamento(3)
                        .diaVencimento(10)
                        .ativo(true)
                        .build()
        ));

        mockMvc.perform(get("/cartoes/listar-dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].descricao").value("Nubank Ultravioleta"))
                .andExpect(jsonPath("$[0].bandeira").value("MASTERCARD"));

        verify(cartaoCreditoService).listarParaGrid(1L);
    }

    @Test
    @DisplayName("Buscar dados para edição de cartão pertencente ao usuário")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_EDITAR")
    void buscar_quandoCartaoDoUsuario_deveRetornarEdicaoDTO() throws Exception {
        CartaoCreditoEdicaoDTO dto = CartaoCreditoEdicaoDTO.builder()
                .id(10L)
                .descricao("Nubank Ultravioleta")
                .bandeira("MASTERCARD")
                .ativo(true)
                .build();

        when(cartaoCreditoService.buscarParaEdicao(10L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/cartoes/buscar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.descricao").value("Nubank Ultravioleta"));
    }

    @Test
    @DisplayName("BDD 16.3 - Buscar cartão de outro usuário retorna 404 (RN02)")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_EDITAR")
    void buscar_quandoCartaoDeOutroUsuario_deveRetornar404() throws Exception {
        when(cartaoCreditoService.buscarParaEdicao(99L, 1L))
                .thenThrow(new RegistroNaoEncontradoException("msg.cartao.nao-encontrado"));

        mockMvc.perform(get("/cartoes/buscar/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BDD 16.4 - Cadastrar cartão com dados válidos retorna sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_INSERIR")
    void inserir_comDadosValidos_deveRetornarOk() throws Exception {
        when(cartaoCreditoService.inserir(any(), eq(1L), eq("user_teste"))).thenReturn(new CartaoCredito());

        mockMvc.perform(post("/cartoes/inserir")
                        .with(csrf())
                        .param("descricao", "Nubank Ultravioleta")
                        .param("bandeira", "MASTERCARD")
                        .param("finalCartao", "1234")
                        .param("diaFechamento", "3")
                        .param("diaVencimento", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(cartaoCreditoService).inserir(any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("BDD 16.5 - Dia de vencimento fora de 1-31 retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_INSERIR")
    void inserir_comDiaVencimentoInvalido_deveRetornar422() throws Exception {
        mockMvc.perform(post("/cartoes/inserir")
                        .with(csrf())
                        .param("descricao", "Nubank Ultravioleta")
                        .param("diaVencimento", "35"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.diaVencimento").exists());

        verify(cartaoCreditoService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("BDD 16.6 - Final do cartão com caractere inválido retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_INSERIR")
    void inserir_comFinalCartaoInvalido_deveRetornar422() throws Exception {
        mockMvc.perform(post("/cartoes/inserir")
                        .with(csrf())
                        .param("descricao", "Nubank Ultravioleta")
                        .param("finalCartao", "12A"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.finalCartao").exists());

        verify(cartaoCreditoService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("BDD 16.7 - Conta de débito de outro usuário retorna 422 no campo contaId")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_INSERIR")
    void inserir_comContaDeOutroUsuario_deveRetornar422() throws Exception {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(9L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/cartoes/inserir")
                        .with(csrf())
                        .param("descricao", "Nubank Ultravioleta")
                        .param("contaId", "9"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.contaId").exists());

        verify(cartaoCreditoService, never()).inserir(any(), any(), any());
    }

    @Test
    @DisplayName("Editar cartão com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_EDITAR")
    void editar_comDadosValidos_deveRetornarOk() throws Exception {
        when(cartaoCreditoService.editar(eq(10L), any(), eq(1L), eq("user_teste"))).thenReturn(new CartaoCredito());

        mockMvc.perform(put("/cartoes/editar/10")
                        .with(csrf())
                        .param("descricao", "Nubank Ultravioleta Atualizado")
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(cartaoCreditoService).editar(eq(10L), any(), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("Editar cartão de outro usuário retorna 404")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_EDITAR")
    void editar_quandoCartaoDeOutroUsuario_deveRetornar404() throws Exception {
        when(cartaoCreditoService.editar(eq(99L), any(), eq(1L), eq("user_teste")))
                .thenThrow(new RegistroNaoEncontradoException("msg.cartao.nao-encontrado"));

        mockMvc.perform(put("/cartoes/editar/99")
                        .with(csrf())
                        .param("descricao", "Qualquer")
                        .param("ativo", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BDD 16.11 - Excluir cartão sem vínculo com sucesso")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_EXCLUIR")
    void excluir_semVinculo_deveRetornarOk() throws Exception {
        mockMvc.perform(delete("/cartoes/excluir/10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(cartaoCreditoService).excluir(10L, 1L, "user_teste");
    }

    @Test
    @DisplayName("BDD 16.9 - Excluir cartão em uso retorna 422 com a oferta de desativar")
    @WithMockUser(username = "user_teste", authorities = "PERM_CARTOES_EXCLUIR")
    void excluir_comVinculo_deveRetornar422() throws Exception {
        org.mockito.Mockito.doThrow(new RegraNegocioException("msg.cartao.em-uso.bloqueada"))
                .when(cartaoCreditoService).excluir(10L, 1L, "user_teste");

        mockMvc.perform(delete("/cartoes/excluir/10")
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    @DisplayName("BDD 16.12 - Listar opções de cartões ativos exige apenas usuário autenticado")
    @WithMockUser(username = "user_teste", authorities = "ROLE_USER")
    void listarOpcoes_usuarioAutenticado_deveRetornarListaJson() throws Exception {
        when(cartaoCreditoService.listarOpcoesCombobox(1L)).thenReturn(List.of(
                new CartaoCreditoOpcaoDTO(1L, "Nubank", "MASTERCARD", "1234", 3, 10)
        ));

        mockMvc.perform(get("/cartoes/opcoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Nubank"));

        verify(cartaoCreditoService).listarOpcoesCombobox(1L);
    }
}
