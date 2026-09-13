package br.com.diegocordeiro.dscproject.config;

import br.com.diegocordeiro.dscproject.config.runner.CargaInicialRunner;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilPermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.perfil.PermissaoCatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CargaInicialRunnerTest {

    @Mock private PerfilRepository perfilRepository;
    @Mock private PermissaoRepository permissaoRepository;
    @Mock private PerfilPermissaoRepository perfilPermissaoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PermissaoCatalogoService permissaoCatalogoService;
    @Mock private PasswordEncoder passwordEncoder;

    @BeforeEach
    void catalogoJaPopulado() {
        when(perfilRepository.findByCodigo(anyString()))
            .thenReturn(Optional.of(new Perfil("ADMIN", "ADMIN", null, true)));
        when(permissaoRepository.findAll())
            .thenReturn(java.util.List.of(new Permissao("X", "X", null, "Usuários")));
        when(perfilPermissaoRepository.existsByPerfilAndPermissao(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hash");
    }

    private CargaInicialRunner runner(String login, String email, String senha, boolean obrigatorio) {
        return new CargaInicialRunner(perfilRepository, permissaoRepository, perfilPermissaoRepository,
            usuarioRepository, permissaoCatalogoService, passwordEncoder, true,
            login, email, senha, "Administrador", obrigatorio);
    }

    @Test
    void semAdminAtivoSemCredencialEObrigatorio_falhaNoBoot() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(0L);

        assertThatThrownBy(() -> runner("", "", "", true).run(null))
            .isInstanceOf(IllegalStateException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void semAdminAtivoSemCredencialENaoObrigatorio_apenasAvisa() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(0L);

        runner("", "", "", false).run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void semAdminAtivoComCredencial_criaAdmin() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(0L);
        when(usuarioRepository.findByLoginOrEmail(any(), any())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        runner("admin", "admin@dsc.com", "s3nha", true).run(null);

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void jaExisteAdminAtivo_naoRecria() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(1L);

        runner("admin", "admin@dsc.com", "s3nha", true).run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginDoAdminJaEmUsoPorOutroUsuario_naoCriaEFalhaSeObrigatorio() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(0L);
        when(usuarioRepository.findByLoginOrEmail(any(), any()))
            .thenReturn(new Usuario());

        assertThatThrownBy(() -> runner("admin", "admin@dsc.com", "s3nha", true).run(null))
            .isInstanceOf(IllegalStateException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void aoSubir_sincronizaOCatalogoDePermissoes() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(1L);

        runner("", "", "", false).run(null);

        verify(permissaoCatalogoService).sincronizar();
    }

    @Test
    void encodaASenhaDoAdmin() {
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(0L);
        when(usuarioRepository.findByLoginOrEmail(any(), any())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        runner("admin", "admin@dsc.com", "s3nha", false).run(null);

        verify(passwordEncoder).encode("s3nha");
    }

    @Test
    void vinculaPermissaoRateioParaAdminEUser() {
        Perfil admin = new Perfil("ADMIN", "ADMIN", null, true);
        Perfil user = new Perfil("USER", "USER", null, true);
        Permissao permRateio = new Permissao("DESPESA_RATEAR_MULTIUSUARIO", "Ratear", null, "Despesas");
        permRateio.setConcedivelPorPlano(false);

        when(perfilRepository.findByCodigo("ADMIN")).thenReturn(Optional.of(admin));
        when(perfilRepository.findByCodigo("USER")).thenReturn(Optional.of(user));
        when(permissaoRepository.findAll()).thenReturn(java.util.List.of(permRateio));
        when(perfilPermissaoRepository.existsByPerfilAndPermissao(any(), any())).thenReturn(false);
        when(usuarioRepository.contarAdminsAtivos()).thenReturn(1L);

        runner("", "", "", false).run(null);

        verify(perfilPermissaoRepository).save(argThat(v ->
                "ADMIN".equals(v.getPerfil().getCodigo()) && "DESPESA_RATEAR_MULTIUSUARIO".equals(v.getPermissao().getCodigo())));
        verify(perfilPermissaoRepository).save(argThat(v ->
                "USER".equals(v.getPerfil().getCodigo()) && "DESPESA_RATEAR_MULTIUSUARIO".equals(v.getPermissao().getCodigo())));
    }
}
