package br.com.diegocordeiro.dscproject.service.usuario;

import br.com.diegocordeiro.dscproject.service.comum.EmailService;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRecuperacaoSenha;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRecuperacaoSenhaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.TokenRecuperacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static br.com.diegocordeiro.dscproject.support.TestFixtures.perfil;
import static br.com.diegocordeiro.dscproject.support.TestFixtures.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioRecuperacaoSenhaRepository recuperacaoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    private RecuperacaoSenhaService service;

    @BeforeEach
    void init() {
        service = new RecuperacaoSenhaService(usuarioRepository, recuperacaoRepository,
            passwordEncoder, emailService, 30, 5, 3, 15, "http://localhost:8080");
        lenient().when(recuperacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(passwordEncoder.encode(any())).thenReturn("$2a$10$novoHash");
    }

    // ---------- RN13 ----------

    @Test
    void solicitar_emailInexistente_naoGeraTokenNemEnvia() {
        when(usuarioRepository.findByEmailAndDataExclusaoIsNull("ninguem@test.com"))
            .thenReturn(Optional.empty());

        service.solicitar("ninguem@test.com");

        verify(recuperacaoRepository, never()).save(any());
        verify(emailService, never()).enviarLinkRecuperacaoSenha(any(), any(), any());
    }

    @Test
    void solicitar_emailExistente_gravaHashInvalidaPendentesEEnvia() {
        Usuario usuario = usuario(1L, "diego", perfil("USER"));
        when(usuarioRepository.findByEmailAndDataExclusaoIsNull(anyString())).thenReturn(Optional.of(usuario));
        when(recuperacaoRepository.countByUsuarioIdAndDataCriacaoAfter(eq(1L), any())).thenReturn(0L);
        UsuarioRecuperacaoSenha pendente = new UsuarioRecuperacaoSenha();
        when(recuperacaoRepository.findByUsuarioIdAndUtilizadoFalseAndDataExclusaoIsNull(1L))
            .thenReturn(List.of(pendente));

        service.solicitar("diego@test.com");

        assertThat(pendente.isUtilizado()).isTrue();

        ArgumentCaptor<UsuarioRecuperacaoSenha> captor = ArgumentCaptor.forClass(UsuarioRecuperacaoSenha.class);
        verify(recuperacaoRepository).save(captor.capture());
        UsuarioRecuperacaoSenha gravado = captor.getValue();
        assertThat(gravado.getTokenHash()).isNotBlank();
        assertThat(gravado.getExpiraEm()).isAfter(Instant.now());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarLinkRecuperacaoSenha(eq("diego@test.com"), eq("Fulano de Tal"), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).contains("/usuarios/recuperar-senha?token=");
    }

    // ---------- RN16 ----------

    @Test
    void solicitar_acimaDoLimiteDeSolicitacoes_naoGeraNovoToken() {
        Usuario usuario = usuario(1L, "diego", perfil("USER"));
        when(usuarioRepository.findByEmailAndDataExclusaoIsNull(anyString())).thenReturn(Optional.of(usuario));
        when(recuperacaoRepository.countByUsuarioIdAndDataCriacaoAfter(eq(1L), any())).thenReturn(3L);

        service.solicitar("diego@test.com");

        verify(recuperacaoRepository, never()).save(any());
        verify(emailService, never()).enviarLinkRecuperacaoSenha(any(), any(), any());
    }

    // ---------- RN14 ----------

    @Test
    void confirmar_tokenInexistente_lancaInvalido() {
        when(recuperacaoRepository.findByTokenHashAndDataExclusaoIsNull(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmar("qualquer", "novaSenha"))
            .isInstanceOf(TokenRecuperacaoException.class)
            .hasMessage("msg.recuperacao.token.invalido");
    }

    @Test
    void confirmar_tokenUtilizado_lancaInvalido() {
        UsuarioRecuperacaoSenha registro = registro(Instant.now().plus(10, ChronoUnit.MINUTES), (short) 0, true);
        when(recuperacaoRepository.findByTokenHashAndDataExclusaoIsNull(anyString())).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.confirmar("tok", "novaSenha"))
            .isInstanceOf(TokenRecuperacaoException.class)
            .hasMessage("msg.recuperacao.token.invalido");
    }

    @Test
    void confirmar_tokenExpirado_lancaExpirado() {
        UsuarioRecuperacaoSenha registro = registro(Instant.now().minus(1, ChronoUnit.MINUTES), (short) 0, false);
        when(recuperacaoRepository.findByTokenHashAndDataExclusaoIsNull(anyString())).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.confirmar("tok", "novaSenha"))
            .isInstanceOf(TokenRecuperacaoException.class)
            .hasMessage("msg.recuperacao.token.expirado");
        assertThat(registro.getTentativas()).isEqualTo((short) 1);
    }

    @Test
    void confirmar_acimaDoLimiteDeTentativas_lancaTentativasExcedidas() {
        UsuarioRecuperacaoSenha registro = registro(Instant.now().plus(10, ChronoUnit.MINUTES), (short) 5, false);
        when(recuperacaoRepository.findByTokenHashAndDataExclusaoIsNull(anyString())).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.confirmar("tok", "novaSenha"))
            .isInstanceOf(TokenRecuperacaoException.class)
            .hasMessage("msg.recuperacao.token.tentativas");
    }

    // ---------- RN15 ----------

    @Test
    void confirmar_tokenValido_reencodaSenhaEMarcaUtilizado() {
        UsuarioRecuperacaoSenha registro = registro(Instant.now().plus(10, ChronoUnit.MINUTES), (short) 0, false);
        when(recuperacaoRepository.findByTokenHashAndDataExclusaoIsNull(anyString())).thenReturn(Optional.of(registro));

        service.confirmar("tok", "novaSenhaForte");

        verify(passwordEncoder).encode("novaSenhaForte");
        assertThat(registro.getUsuario().getSenha()).isEqualTo("$2a$10$novoHash");
        assertThat(registro.isUtilizado()).isTrue();
        verify(usuarioRepository).save(registro.getUsuario());
    }

    private UsuarioRecuperacaoSenha registro(Instant expiraEm, short tentativas, boolean utilizado) {
        UsuarioRecuperacaoSenha registro = new UsuarioRecuperacaoSenha();
        registro.setUsuario(usuario(1L, "diego", perfil("USER")));
        registro.setTokenHash("hash");
        registro.setExpiraEm(expiraEm);
        registro.setTentativas(tentativas);
        registro.setUtilizado(utilizado);
        return registro;
    }
}
