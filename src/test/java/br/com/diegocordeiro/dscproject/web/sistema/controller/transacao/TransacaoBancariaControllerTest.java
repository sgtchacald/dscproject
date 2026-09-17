package br.com.diegocordeiro.dscproject.web.sistema.controller.transacao;

import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaTotaisDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.conta.ContaService;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.transacao.TransacaoBancariaImportacaoService;
import br.com.diegocordeiro.dscproject.service.transacao.TransacaoBancariaService;
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

@WebMvcTest(TransacaoBancariaController.class)
@Import(SecurityConfig.class)
class TransacaoBancariaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private TransacaoBancariaService transacaoBancariaService;

    @MockitoBean
    private TransacaoBancariaImportacaoService transacaoBancariaImportacaoService;

    @MockitoBean
    private TransacaoBancariaRepository transacaoBancariaRepository;

    @MockitoBean
    private ContaRepository contaRepository;

    @MockitoBean
    private ContaService contaService;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

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
    @DisplayName("Deve carregar tela de listagem quando usuario possui permissao TRANSACOES_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_TRANSACOES_LISTAR")
    void deveCarregarTelaListagemComPermissao() throws Exception {
        when(contaService.listarOpcoesCombobox(1L)).thenReturn(List.of());

        mockMvc.perform(get("/transacoes-bancarias/listar"))
            .andExpect(status().isOk())
            .andExpect(view().name("sistema/modulos/transacao-bancaria/listar"));
    }

    @Test
    @DisplayName("Deve retornar 403 ao tentar listar transacoes sem permissao")
    @WithMockUser(username = "user_teste", authorities = "OUTRA_PERMISSAO")
    void deveNegarListagemSemPermissao() throws Exception {
        mockMvc.perform(get("/transacoes-bancarias/listar"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve buscar transacao para edicao com autoridade TRANSACOES_EDITAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_TRANSACOES_EDITAR")
    void deveBuscarParaEdicao() throws Exception {
        TransacaoBancariaEdicaoDTO dto = TransacaoBancariaEdicaoDTO.builder()
            .id(10L)
            .descricao("Transação Teste")
            .valor(new BigDecimal("100.00"))
            .natureza(NaturezaMovimento.CREDITO)
            .dataLancamento(LocalDate.of(2026, 9, 10))
            .competencia(YearMonth.of(2026, 9))
            .contaId(1L)
            .origem(OrigemLancamento.MANUAL)
            .pagamentoFatura(false)
            .build();

        when(transacaoBancariaService.buscarParaEdicao(10L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/transacoes-bancarias/buscar/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.descricao").value("Transação Teste"))
            .andExpect(jsonPath("$.natureza").value("CREDITO"));
    }

    @Test
    @DisplayName("Deve excluir transacao com autoridade TRANSACOES_EXCLUIR")
    @WithMockUser(username = "user_teste", authorities = "PERM_TRANSACOES_EXCLUIR")
    void deveExcluirComSucesso() throws Exception {
        mockMvc.perform(delete("/transacoes-bancarias/excluir/10").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true));
    }
}
