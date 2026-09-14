package br.com.diegocordeiro.dscproject.service.perfil;

import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;

import static br.com.diegocordeiro.dscproject.support.TestFixtures.perfil;
import static br.com.diegocordeiro.dscproject.support.TestFixtures.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorizacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutorizacaoService autorizacaoService;

    @Test
    void loadUserByUsername_encontraPorLogin() {
        Usuario usuario = usuario(1L, "chacalsgt", perfil("USER"));
        when(usuarioRepository.findByLoginOrEmail("chacalsgt", "chacalsgt")).thenReturn(usuario);

        UserDetails result = autorizacaoService.loadUserByUsername("chacalsgt");

        assertThat(result.getUsername()).isEqualTo("chacalsgt");
    }

    @Test
    void loadUserByUsername_lancaExcecaoSeNaoEncontrar() {
        when(usuarioRepository.findByLoginOrEmail("naoexiste", "naoexiste")).thenReturn(null);

        assertThatThrownBy(() -> autorizacaoService.loadUserByUsername("naoexiste"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("naoexiste");
    }

    @Test
    void loadUserByUsername_usuarioExcluido_recusaComMensagemGenerica() {
        Usuario excluido = usuario(2L, "antigo", perfil("USER"));
        excluido.setDataExclusao(Instant.now());
        when(usuarioRepository.findByLoginOrEmail("antigo", "antigo")).thenReturn(excluido);

        assertThatThrownBy(() -> autorizacaoService.loadUserByUsername("antigo"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
