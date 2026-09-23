package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialExibicaoDTO;
import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceCredencialRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderFactory;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenFinanceCredencialServiceTest {

    @Mock
    private OpenFinanceCredencialRepository credencialRepository;
    @Mock
    private OpenFinanceProvedorRepository provedorRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OpenFinanceCryptoService cryptoService;
    @Mock
    private OpenFinanceProviderFactory providerFactory;
    @Mock
    private OpenFinanceProviderStrategy providerStrategy;

    private OpenFinanceCredencialService service;

    @BeforeEach
    void setUp() {
        service = new OpenFinanceCredencialService(
                credencialRepository,
                provedorRepository,
                usuarioRepository,
                cryptoService,
                providerFactory
        );
    }

    @Test
    @DisplayName("Salvar credencial com validação bem sucedida cifra o secret e persiste")
    void salvarCredencial_valida_cifraEPersiste() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        OpenFinanceProvedor provedor = new OpenFinanceProvedor();
        provedor.setId(10L);
        provedor.setCodigo("PLUGGY");
        provedor.setNome("Pluggy");

        OpenFinanceCredencialDTO dto = new OpenFinanceCredencialDTO();
        dto.setProvedorId(10L);
        dto.setClientId("client-123");
        dto.setClientSecret("secret-456");
        dto.setAmbiente(AmbienteCredencial.PRODUCTION);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(provedorRepository.findById(10L)).thenReturn(Optional.of(provedor));
        when(credencialRepository.findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(usuarioId, 10L)).thenReturn(Optional.empty());
        when(providerFactory.obterStrategy("PLUGGY")).thenReturn(providerStrategy);
        when(providerStrategy.validarCredenciais("client-123", "secret-456", AmbienteCredencial.PRODUCTION)).thenReturn(true);
        when(cryptoService.encrypt("secret-456")).thenReturn("secret-cifrado-aes256");
        when(credencialRepository.save(any(OpenFinanceCredencial.class))).thenAnswer(inv -> inv.getArgument(0));

        OpenFinanceCredencial salva = service.salvarCredencial(usuarioId, dto);

        assertThat(salva).isNotNull();
        assertThat(salva.getClientId()).isEqualTo("client-123");
        assertThat(salva.getClientSecret()).isEqualTo("secret-cifrado-aes256");
        assertThat(salva.isAtivo()).isTrue();
        verify(credencialRepository).save(any(OpenFinanceCredencial.class));
    }

    @Test
    @DisplayName("Salvar credencial rejeitada pelo provedor lança RegraNegocioException")
    void salvarCredencial_invalidaNoProvedor_lancaExcecao() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        OpenFinanceProvedor provedor = new OpenFinanceProvedor();
        provedor.setId(10L);
        provedor.setCodigo("PLUGGY");

        OpenFinanceCredencialDTO dto = new OpenFinanceCredencialDTO();
        dto.setProvedorId(10L);
        dto.setClientId("client-invalido");
        dto.setClientSecret("secret-invalido");
        dto.setAmbiente(AmbienteCredencial.SANDBOX);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(provedorRepository.findById(10L)).thenReturn(Optional.of(provedor));
        when(credencialRepository.findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(usuarioId, 10L)).thenReturn(Optional.empty());
        when(providerFactory.obterStrategy("PLUGGY")).thenReturn(providerStrategy);
        when(providerStrategy.validarCredenciais("client-invalido", "secret-invalido", AmbienteCredencial.SANDBOX)).thenReturn(false);

        assertThatThrownBy(() -> service.salvarCredencial(usuarioId, dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("openfinance.credencial.invalida");
    }

    @Test
    @DisplayName("Buscar credencial para exibição retorna secret mascarado")
    void buscarCredencialExibicao_retornaSecretMascarado() {
        Long usuarioId = 1L;
        OpenFinanceProvedor provedor = new OpenFinanceProvedor();
        provedor.setId(10L);
        provedor.setCodigo("PLUGGY");
        provedor.setNome("Pluggy");

        OpenFinanceCredencial cred = new OpenFinanceCredencial();
        cred.setId(5L);
        cred.setProvedor(provedor);
        cred.setClientId("client-xyz");
        cred.setClientSecret("segredo-cifrado");
        cred.setAmbiente(AmbienteCredencial.PRODUCTION);
        cred.setAtivo(true);

        when(credencialRepository.findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(usuarioId, 10L)).thenReturn(Optional.of(cred));

        OpenFinanceCredencialExibicaoDTO exibicao = service.buscarCredencialExibicao(usuarioId, 10L);

        assertThat(exibicao.isPossuiSecret()).isTrue();
        assertThat(exibicao.getSecretMascarado()).isEqualTo("********");
        assertThat(exibicao.getClientId()).isEqualTo("client-xyz");
    }
}
