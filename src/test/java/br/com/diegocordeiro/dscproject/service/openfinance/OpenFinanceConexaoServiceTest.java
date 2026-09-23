package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceConexaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyItemDTO;
import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.enums.StatusConsentimento;
import br.com.diegocordeiro.dscproject.enums.TipoContaExterna;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConsentimento;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceContaExterna;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConexaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConsentimentoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceContaExternaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderFactory;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenFinanceConexaoServiceTest {

    @Mock
    private OpenFinanceConexaoRepository conexaoRepository;
    @Mock
    private OpenFinanceConsentimentoRepository consentimentoRepository;
    @Mock
    private OpenFinanceContaExternaRepository contaExternaRepository;
    @Mock
    private OpenFinanceInstituicaoProvedorRepository instituicaoProvedorRepository;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private CartaoCreditoRepository cartaoCreditoRepository;
    @Mock
    private OpenFinanceCredencialService credencialService;
    @Mock
    private OpenFinanceSincronizacaoService sincronizacaoService;
    @Mock
    private OpenFinanceProviderFactory providerFactory;
    @Mock
    private OpenFinanceProviderStrategy providerStrategy;

    private OpenFinanceConexaoService service;

    @BeforeEach
    void setUp() {
        service = new OpenFinanceConexaoService(
                conexaoRepository,
                consentimentoRepository,
                contaExternaRepository,
                instituicaoProvedorRepository,
                contaRepository,
                cartaoCreditoRepository,
                credencialService,
                sincronizacaoService,
                providerFactory
        );
    }

    @Test
    @DisplayName("Listar conexões do usuário retorna DTO enriquecido com status e consentimento")
    void listarConexoes_retornaListaDTO() {
        Long usuarioId = 1L;

        InstituicaoFinanceira banco = new InstituicaoFinanceira();
        banco.setId(10L);
        banco.setNome("Nubank");
        banco.setCodigo("260");

        OpenFinanceProvedor provedor = new OpenFinanceProvedor();
        provedor.setId(1L);
        provedor.setNome("Pluggy");
        provedor.setCodigo("PLUGGY");

        OpenFinanceConexao c = new OpenFinanceConexao();
        c.setId(100L);
        c.setIdExterno("item-nubank-1");
        c.setStatus(StatusConexao.ATUALIZADO);
        c.setInstituicao(banco);
        c.setProvedor(provedor);

        OpenFinanceConsentimento cs = new OpenFinanceConsentimento();
        cs.setStatus(StatusConsentimento.ATIVO);
        cs.setDataExpiracao(LocalDateTime.now().plusMonths(6));
        c.setConsentimento(cs);

        when(conexaoRepository.findByUsuarioIdAndDataExclusaoIsNull(usuarioId)).thenReturn(List.of(c));

        List<OpenFinanceConexaoGridDTO> grid = service.listarConexoes(usuarioId);

        assertThat(grid).hasSize(1);
        OpenFinanceConexaoGridDTO dto = grid.get(0);
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getInstituicaoNome()).isEqualTo("Nubank");
        assertThat(dto.getStatus()).isEqualTo(StatusConexao.ATUALIZADO);
        assertThat(dto.getStatusBadgeClass()).isEqualTo("bg-success-lt");
    }

    @Test
    @DisplayName("Processar callback cria conexão, espelho de consentimento e dispara sincronização inicial")
    void processarCallback_criaConexaoEConsentimento() {
        Long usuarioId = 1L;
        String itemId = "item-pluggy-novo";

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        OpenFinanceProvedor provedor = new OpenFinanceProvedor();
        provedor.setId(1L);
        provedor.setCodigo("PLUGGY");

        OpenFinanceCredencial cred = new OpenFinanceCredencial();
        cred.setUsuario(usuario);
        cred.setProvedor(provedor);
        cred.setClientId("cid");
        cred.setClientSecret("secret-cifrado");
        cred.setAmbiente(AmbienteCredencial.PRODUCTION);

        PluggyItemDTO itemDTO = new PluggyItemDTO();
        itemDTO.setId(itemId);
        itemDTO.setStatus("UPDATED");

        when(credencialService.obterCredencialAtivaPorProvedorCodigo(usuarioId, "PLUGGY")).thenReturn(cred);
        when(credencialService.obterClientSecretDecifrado(cred)).thenReturn("secret-plano");
        when(providerFactory.obterStrategy("PLUGGY")).thenReturn(providerStrategy);
        when(providerStrategy.buscarItem("cid", "secret-plano", AmbienteCredencial.PRODUCTION, itemId)).thenReturn(itemDTO);
        when(conexaoRepository.findByIdExternoAndProvedorIdAndDataExclusaoIsNull(itemId, 1L)).thenReturn(Optional.empty());
        when(conexaoRepository.save(any(OpenFinanceConexao.class))).thenAnswer(inv -> {
            OpenFinanceConexao c = inv.getArgument(0);
            c.setId(200L);
            return c;
        });

        OpenFinanceConexao resultado = service.processarCallback(usuarioId, itemId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(200L);
        assertThat(resultado.getStatus()).isEqualTo(StatusConexao.ATUALIZADO);

        verify(consentimentoRepository).save(any(OpenFinanceConsentimento.class));
        verify(sincronizacaoService).sincronizarConexaoAssincrona(eq(200L), any());
    }

    @Test
    @DisplayName("Desconectar revoga item no agregador e aplica exclusão lógica no registro local")
    void desconectar_revogaNoProvedorEAplicaExclusaoLogica() {
        Long usuarioId = 1L;
        Long conexaoId = 100L;

        OpenFinanceProvedor provedor = new OpenFinanceProvedor();
        provedor.setCodigo("PLUGGY");

        OpenFinanceCredencial cred = new OpenFinanceCredencial();
        cred.setClientId("cid");
        cred.setAmbiente(AmbienteCredencial.PRODUCTION);

        OpenFinanceConexao c = new OpenFinanceConexao();
        c.setId(conexaoId);
        c.setIdExterno("item-revogar");
        c.setProvedor(provedor);
        c.setCredencial(cred);

        when(conexaoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(conexaoId, usuarioId)).thenReturn(Optional.of(c));
        when(credencialService.obterClientSecretDecifrado(cred)).thenReturn("secret");
        when(providerFactory.obterStrategy("PLUGGY")).thenReturn(providerStrategy);
        when(contaExternaRepository.findByConexaoIdAndDataExclusaoIsNull(conexaoId)).thenReturn(List.of());

        service.desconectar(usuarioId, conexaoId);

        verify(providerStrategy).revogarItem("cid", "secret", AmbienteCredencial.PRODUCTION, "item-revogar");
        assertThat(c.getDataExclusao()).isNotNull();
        verify(conexaoRepository).save(c);
    }

    @Test
    @DisplayName("Vincular conta externa BANK a cartão de crédito lança RegraNegocioException de tipo incompatível")
    void vincular_contaBankEmCartao_lancaExcecao() {
        Long usuarioId = 1L;
        Long ceId = 50L;

        OpenFinanceContaExterna ce = new OpenFinanceContaExterna();
        ce.setId(ceId);
        ce.setTipo(TipoContaExterna.BANK);

        when(contaExternaRepository.findByIdAndConexaoUsuarioIdAndDataExclusaoIsNull(ceId, usuarioId)).thenReturn(Optional.of(ce));

        assertThatThrownBy(() -> service.vincularContaExterna(usuarioId, ceId, null, 10L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("openfinance.vinculo.tipo-incompativel");
    }

    @Test
    @DisplayName("Vincular conta externa BANK a Conta do sistema vincula com sucesso")
    void vincular_contaBankEmContaValida_sucesso() {
        Long usuarioId = 1L;
        Long ceId = 50L;
        Long ctaId = 10L;

        OpenFinanceContaExterna ce = new OpenFinanceContaExterna();
        ce.setId(ceId);
        ce.setTipo(TipoContaExterna.BANK);

        Conta conta = new Conta();
        conta.setId(ctaId);

        when(contaExternaRepository.findByIdAndConexaoUsuarioIdAndDataExclusaoIsNull(ceId, usuarioId)).thenReturn(Optional.of(ce));
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(ctaId, usuarioId)).thenReturn(Optional.of(conta));
        when(contaExternaRepository.save(any(OpenFinanceContaExterna.class))).thenAnswer(inv -> inv.getArgument(0));

        OpenFinanceContaExterna vinculada = service.vincularContaExterna(usuarioId, ceId, ctaId, null);

        assertThat(vinculada.getConta()).isEqualTo(conta);
        assertThat(vinculada.getCartaoCredito()).isNull();
    }
}
