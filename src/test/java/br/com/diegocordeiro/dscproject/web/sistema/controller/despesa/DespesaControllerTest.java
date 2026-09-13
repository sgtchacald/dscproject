package br.com.diegocordeiro.dscproject.web.sistema.controller.despesa;

import br.com.diegocordeiro.dscproject.service.despesa.DespesaImportacaoService;
import br.com.diegocordeiro.dscproject.config.SecurityConfig;
import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRapidoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRateioDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaGridDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.UsuarioRateioDTO;
import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.contato.ContatoRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.perfil.AutorizacaoService;
import br.com.diegocordeiro.dscproject.service.despesa.DespesaService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DespesaController.class)
@Import(SecurityConfig.class)
class DespesaControllerTest {

    private MockMvc mockMvc;

    @MockitoBean private DespesaService despesaService;
    @MockitoBean private DespesaRepository despesaRepository;
    @MockitoBean private ContaRepository contaRepository;
    @MockitoBean private CartaoCreditoRepository cartaoCreditoRepository;
    @MockitoBean private CategoriaRepository categoriaRepository;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private ContatoRepository contatoRepository;
    @MockitoBean private AutorizacaoService autorizacaoService;
    @MockitoBean private br.com.diegocordeiro.dscproject.service.despesa.DespesaImportacaoService despesaImportacaoService;
    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

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
    @DisplayName("EDP01 - Listar view com autoridade PERM_DESPESAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_LISTAR")
    void listar_comPermissao_deveRetornar200EViewListar() throws Exception {
        mockMvc.perform(get("/despesas/listar"))
                .andExpect(status().isOk())
                .andExpect(view().name("sistema/modulos/despesa/listar"))
                .andExpect(model().attributeExists("podeRatear"));
    }

    @Test
    @DisplayName("EDP01/EDP02 - Bloquear acesso sem PERM_DESPESAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "ROLE_USER")
    void listar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/despesas/listar"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/despesas/listar-dados"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP02 - Listar dados com PERM_DESPESAS_LISTAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_LISTAR")
    void listarDados_comPermissao_deveRetornar200() throws Exception {
        when(despesaService.listarParaGrid(1L)).thenReturn(List.of());

        mockMvc.perform(get("/despesas/listar-dados"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("EDP03 - Buscar por ID com PERM_DESPESAS_EDITAR")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void buscar_comPermissao_despesaExiste_retorna200() throws Exception {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setNome("Internet");
        d.setValor(new BigDecimal("100.00"));
        d.setCompetencia(YearMonth.of(2026, 9));
        d.setOrigem(OrigemLancamento.MANUAL);
        d.setMeioPagamento(MeioPagamento.DEBITO);
        d.setStatusPagamento(StatusPagamento.NAO);
        d.setRateios(new ArrayList<>());

        when(despesaService.buscarParaEdicao(10L, 1L)).thenReturn(new DespesaEdicaoDTO(d));

        mockMvc.perform(get("/despesas/buscar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Internet"));
    }

    @Test
    @DisplayName("EDP03 - Buscar com ID inexistente retorna 404")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void buscar_naoEncontrado_retorna404() throws Exception {
        when(despesaService.buscarParaEdicao(999L, 1L)).thenThrow(new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));

        mockMvc.perform(get("/despesas/buscar/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("EDP04 - Inserir despesa válida com PERM_DESPESAS_INSERIR retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_INSERIR")
    void inserir_valido_retorna200() throws Exception {
        Conta conta = new Conta();
        conta.setId(10L);
        conta.setAtivo(true);
        conta.setTipo(TipoConta.CORRENTE);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(conta));

        Despesa criada = new Despesa();
        criada.setId(1L);
        criada.setParcelada(false);
        when(despesaService.inserir(any(), eq(1L), eq("user_teste"), eq(false))).thenReturn(criada);

        mockMvc.perform(post("/despesas/inserir")
                        .with(csrf())
                        .param("nome", "Aluguel")
                        .param("valor", "1500.00")
                        .param("dataLancamento", "2026-09-01")
                        .param("competencia", "2026-09")
                        .param("formaPagamento", "CONTA")
                        .param("contaId", "10")
                        .param("statusPagamento", "NAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("EDP04 - Inserir dados inválidos retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_INSERIR")
    void inserir_invalido_retorna422() throws Exception {
        mockMvc.perform(post("/despesas/inserir")
                        .with(csrf())
                        .param("nome", "")
                        .param("valor", "-5.00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos").exists());
    }

    @Test
    @DisplayName("EDP05 - Editar despesa com PERM_DESPESAS_EDITAR retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void editar_valido_retorna200() throws Exception {
        Conta conta = new Conta();
        conta.setId(10L);
        conta.setAtivo(true);
        conta.setTipo(TipoConta.CORRENTE);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(conta));

        Despesa existente = new Despesa();
        existente.setId(5L);
        existente.setOrigem(OrigemLancamento.MANUAL);
        when(despesaRepository.buscarPorIdEUsuario(5L, 1L)).thenReturn(Optional.of(existente));
        when(despesaService.buscarPorIdEUsuario(5L, 1L)).thenReturn(existente);

        mockMvc.perform(put("/despesas/editar/5")
                        .with(csrf())
                        .param("nome", "Aluguel Atualizado")
                        .param("valor", "1600.00")
                        .param("dataLancamento", "2026-09-01")
                        .param("competencia", "2026-09")
                        .param("formaPagamento", "CONTA")
                        .param("contaId", "10")
                        .param("statusPagamento", "NAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("EDP06 - Excluir despesa com PERM_DESPESAS_EXCLUIR retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EXCLUIR")
    void excluir_comPermissao_retorna200() throws Exception {
        doNothing().when(despesaService).excluir(5L, 1L, "user_teste");

        mockMvc.perform(delete("/despesas/excluir/5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("EDP07 - Registrar pagamento individual com PERM_DESPESAS_PAGAR retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_PAGAR")
    void registrarPagamento_comPermissao_retorna200() throws Exception {
        doNothing().when(despesaService).registrarPagamento(eq(5L), any(LocalDate.class), eq(1L), eq("user_teste"));

        mockMvc.perform(put("/despesas/registrar-pagamento/5")
                        .with(csrf())
                        .param("dataPagamento", "2026-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("EDP08 - Registrar pagamento em lote com PERM_DESPESAS_PAGAR retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_PAGAR")
    void registrarPagamentoLote_comPermissao_retorna200() throws Exception {
        when(despesaService.registrarPagamentoLote(any(), any(LocalDate.class), eq(1L), eq("user_teste"))).thenReturn(2);

        mockMvc.perform(post("/despesas/registrar-pagamento-lote")
                        .with(csrf())
                        .param("ids", "1", "2")
                        .param("dataPagamento", "2026-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("EDP09 - Acerto rateio com PERM_DESPESA_RATEAR_MULTIUSUARIO retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESA_RATEAR_MULTIUSUARIO")
    void acertoRateio_comPermissao_retorna200() throws Exception {
        mockMvc.perform(put("/despesas/1/rateio-acerto")
                        .with(csrf())
                        .param("usuarioId", "2")
                        .param("acertado", "true")
                        .param("dataAcerto", "2026-09-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("EDP09 - Acerto rateio sem PERM_DESPESA_RATEAR_MULTIUSUARIO retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void acertoRateio_semPermissao_retorna403() throws Exception {
        mockMvc.perform(put("/despesas/1/rateio-acerto")
                        .with(csrf())
                        .param("usuarioId", "2")
                        .param("acertado", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP10 - Buscar usuários rateio com autoridade retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESA_RATEAR_MULTIUSUARIO")
    void buscarUsuariosRateio_comPermissao_retorna200() throws Exception {
        when(despesaService.buscarUsuariosParaRateio("diego", 1L)).thenReturn(List.of());

        mockMvc.perform(get("/despesas/usuarios-rateio").param("termo", "diego"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("EDP10 - Buscar usuários rateio sem autoridade retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void buscarUsuariosRateio_semPermissao_retorna403() throws Exception {
        mockMvc.perform(get("/despesas/usuarios-rateio").param("termo", "diego"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP10 - Buscar contatos rateio com autoridade retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESA_RATEAR_MULTIUSUARIO")
    void buscarContatosRateio_comPermissao_retorna200() throws Exception {
        when(despesaService.buscarContatosParaRateio("carlos", 1L)).thenReturn(List.of());

        mockMvc.perform(get("/despesas/contatos-rateio").param("termo", "carlos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("EDP10 - Buscar contatos rateio sem autoridade retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void buscarContatosRateio_semPermissao_retorna403() throws Exception {
        mockMvc.perform(get("/despesas/contatos-rateio").param("termo", "carlos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP11 - Cadastrar contato rápido com autoridade retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESA_RATEAR_MULTIUSUARIO")
    void cadastrarContatoRapido_comPermissao_retorna200() throws Exception {
        ContatoRateioDTO contatoMock = new ContatoRateioDTO();
        contatoMock.setId(10L);
        contatoMock.setNome("Mariana");
        when(despesaService.cadastrarContatoRapido(any(ContatoRapidoDTO.class), eq(1L), eq("user_teste")))
                .thenReturn(contatoMock);

        mockMvc.perform(post("/despesas/contatos-rapido")
                        .with(csrf())
                        .param("nome", "Mariana")
                        .param("email", "mariana@teste.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.contato.nome").value("Mariana"));
    }

    @Test
    @DisplayName("EDP11 - Cadastrar contato rápido sem autoridade retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void cadastrarContatoRapido_semPermissao_retorna403() throws Exception {
        mockMvc.perform(post("/despesas/contatos-rapido")
                        .with(csrf())
                        .param("nome", "Mariana"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP12 / RN26 - Duplicar sem despesaIds retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_INSERIR")
    void duplicar_semIds_deveRetornar422() throws Exception {
        mockMvc.perform(post("/despesas/duplicar").with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosNegocio.despesaIds").exists());

        verify(despesaService, never()).duplicar(any(), any(), any(), any());
    }

    @Test
    @DisplayName("EDP12 / RN26 - Duplicar com sucesso retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_INSERIR")
    void duplicar_comSucesso_deveRetornar200() throws Exception {
        mockMvc.perform(post("/despesas/duplicar")
                        .param("despesaIds", "1", "2")
                        .param("competenciaDestino", "2026-10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").exists());

        verify(despesaService).duplicar(eq(List.of(1L, 2L)), eq("2026-10"), eq(1L), eq("user_teste"));
    }

    @Test
    @DisplayName("EDP12 / RN01 - Duplicar sem permissão retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_LISTAR")
    void duplicar_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(post("/despesas/duplicar")
                        .param("despesaIds", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP13 / RN27 - Atualizar valor com valor zero ou negativo retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void atualizarValor_comValorInvalido_deveRetornar422() throws Exception {
        mockMvc.perform(patch("/despesas/10/valor")
                        .param("valor", "0.00")
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.valor").exists());

        verify(despesaService, never()).atualizarValor(any(), any(), any(), any());
    }

    @Test
    @DisplayName("EDP13 / RN27 - Atualizar valor com sucesso retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void atualizarValor_comSucesso_deveRetornar200() throws Exception {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setValor(new BigDecimal("180.00"));
        when(despesaService.atualizarValor(10L, new BigDecimal("180.00"), 1L, "user_teste")).thenReturn(d);

        mockMvc.perform(patch("/despesas/10/valor")
                        .param("valor", "180.00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.valor").value(180.00));
    }

    @Test
    @DisplayName("EDP15 / RN32 - Atualizar competência com formato inválido retorna 422")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void atualizarCompetencia_comCompetenciaInvalida_deveRetornar422() throws Exception {
        mockMvc.perform(patch("/despesas/10/competencia")
                        .param("competencia", "2026/08")
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.errosCampos.competencia").exists());

        verify(despesaService, never()).atualizarCompetencia(any(), any(), any(), any());
    }

    @Test
    @DisplayName("EDP15 / RN32 - Atualizar competência com sucesso retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void atualizarCompetencia_comSucesso_deveRetornar200() throws Exception {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setCompetencia(YearMonth.of(2026, 10));
        when(despesaService.atualizarCompetencia(10L, "2026-10", 1L, "user_teste")).thenReturn(d);

        mockMvc.perform(patch("/despesas/10/competencia")
                        .param("competencia", "2026-10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.competencia").value("2026-10"));
    }

    @Test
    @DisplayName("EDP16 / RN33 - Atualizar categoria com sucesso retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void atualizarCategoria_comSucesso_deveRetornar200() throws Exception {
        Categoria cat = new Categoria();
        cat.setId(2L);
        cat.setNome("Alimentação");

        Despesa d = new Despesa();
        d.setId(10L);
        d.setCategoria(cat);
        when(despesaService.atualizarCategoria(10L, 2L, 1L, "user_teste")).thenReturn(d);

        mockMvc.perform(patch("/despesas/10/categoria")
                        .param("categoriaId", "2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.categoriaId").value(2))
                .andExpect(jsonPath("$.categoriaNome").value("Alimentação"));
    }

    @Test
    @DisplayName("EDP16 / RN33 - Atualizar categoria sem categoriaId (desvincular) retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_EDITAR")
    void atualizarCategoria_desvincular_deveRetornar200() throws Exception {
        Despesa d = new Despesa();
        d.setId(10L);
        d.setCategoria(null);
        when(despesaService.atualizarCategoria(10L, null, 1L, "user_teste")).thenReturn(d);

        mockMvc.perform(patch("/despesas/10/categoria")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.categoriaId").isEmpty())
                .andExpect(jsonPath("$.categoriaNome").value(""));
    }

    @Test
    @DisplayName("EDP16 / RN01 - Atualizar categoria sem permissão retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_LISTAR")
    void atualizarCategoria_semPermissao_deveRetornar403() throws Exception {
        mockMvc.perform(patch("/despesas/10/categoria")
                        .param("categoriaId", "2")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EDP14 / RN28 - Importar extrato com sucesso retorna 200")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_IMPORTAR")
    void importarExtrato_comSucesso_deveRetornar200() throws Exception {
        org.springframework.mock.web.MockMultipartFile arq = new org.springframework.mock.web.MockMultipartFile(
                "arquivo", "fatura.csv", "text/csv", "data;valor".getBytes());

        when(despesaImportacaoService.importarFatura(any(), eq(1L), eq("2026-09"), eq("C6BANK"), any(), any(), eq(1L), eq("user_teste")))
                .thenReturn(5);

        mockMvc.perform(multipart("/despesas/importar-extrato")
                        .file(arq)
                        .param("cartaoId", "1")
                        .param("competencia", "2026-09")
                        .param("formato", "C6BANK")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.total").value(5));
    }

    @Test
    @DisplayName("EDP14 / RN01 - Importar extrato sem permissão retorna 403")
    @WithMockUser(username = "user_teste", authorities = "PERM_DESPESAS_LISTAR")
    void importarExtrato_semPermissao_deveRetornar403() throws Exception {
        org.springframework.mock.web.MockMultipartFile arq = new org.springframework.mock.web.MockMultipartFile(
                "arquivo", "fatura.csv", "text/csv", "data;valor".getBytes());

        mockMvc.perform(multipart("/despesas/importar-extrato")
                        .file(arq)
                        .param("cartaoId", "1")
                        .param("competencia", "2026-09")
                        .param("formato", "C6BANK")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
