package br.com.diegocordeiro.dscproject.model.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;

import static br.com.diegocordeiro.dscproject.support.TestFixtures.perfil;
import static br.com.diegocordeiro.dscproject.support.TestFixtures.usuario;
import static org.assertj.core.api.Assertions.assertThat;

class UsuarioUserDetailsTest {

    @Test
    void getPassword_deveRetornarSenha() {
        Usuario usuario = new Usuario();
        usuario.setSenha("$2a$10$hash");

        assertThat(usuario.getPassword()).isEqualTo("$2a$10$hash");
    }

    @Test
    void getUsername_deveRetornarLogin() {
        Usuario usuario = new Usuario();
        usuario.setLogin("chacalsgt");

        assertThat(usuario.getUsername()).isEqualTo("chacalsgt");
    }

    @Test
    void getAuthorities_incluiRoleDoPerfilEUmaAutoridadePorPermissao() {
        Usuario usuario = usuario(1L, "admin",
            perfil("ADMIN", "USUARIOS_LISTAR", "USUARIOS_INSERIR", "USUARIOS_EXCLUIR"));

        Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder(
                "ROLE_ADMIN",
                "PERM_USUARIOS_LISTAR",
                "PERM_USUARIOS_INSERIR",
                "PERM_USUARIOS_EXCLUIR");
    }

    @Test
    void getAuthorities_perfilSemPermissoes_apenasRole() {
        Usuario usuario = usuario(2L, "comum", perfil("USER"));

        Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER");
    }

    @Test
    void isEnabled_falsoQuandoExcluido() {
        Usuario usuario = usuario(3L, "excluido", perfil("USER"));
        assertThat(usuario.isEnabled()).isTrue();

        usuario.setDataExclusao(Instant.now());
        assertThat(usuario.isEnabled()).isFalse();
    }
}
