package br.com.diegocordeiro.dscproject.repository.usuario;

import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.config.JpaAuditingConfig;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"dev", "test"})
@Import(JpaAuditingConfig.class)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PerfilRepository perfilRepository;

    private Perfil admin;
    private Perfil user;

    @BeforeEach
    void carga() {
        admin = perfilRepository.findByCodigo("ADMIN")
            .orElseGet(() -> perfilRepository.save(new Perfil("ADMIN", "Administrador", null, true)));
        user = perfilRepository.findByCodigo("USER")
            .orElseGet(() -> perfilRepository.save(new Perfil("USER", "Usuário", null, true)));
    }

    private Usuario novo(String login, Perfil perfil) {
        Usuario u = new Usuario();
        u.setNome(login);
        u.setGenero(Genero.OUTRO);
        u.setNascimento(LocalDate.of(1990, 1, 1));
        u.setEmail(login + "@test.com");
        u.setLogin(login);
        u.setSenha("$2a$10$hash");
        u.setPerfil(perfil);
        return u;
    }

    // ---------- C2 / RN04 ----------

    @Test
    void contarPorLoginOuEmail_encontraPorLoginEPorEmail() {
        usuarioRepository.save(novo("diego", user));

        assertThat(usuarioRepository.contarPorLoginOuEmail("diego", null)).isEqualTo(1L);
        assertThat(usuarioRepository.contarPorLoginOuEmail("diego@test.com", null)).isEqualTo(1L);
        assertThat(usuarioRepository.contarPorLoginOuEmail("outro", null)).isZero();
    }

    @Test
    void contarPorLoginOuEmail_ignoraOProprioRegistro() {
        Usuario diego = usuarioRepository.save(novo("diego", user));

        assertThat(usuarioRepository.contarPorLoginOuEmail("diego", diego.getId())).isZero();
    }

    @Test
    void contarPorLoginOuEmail_ignoraUsuariosExcluidos() {
        Usuario diego = novo("diego", user);
        diego.setDataExclusao(Instant.now());
        diego.setExcluidoPor("sistema");
        usuarioRepository.save(diego);

        assertThat(usuarioRepository.contarPorLoginOuEmail("diego", null)).isZero();
    }

    // ---------- C4 / RN11 ----------

    @Test
    void contarAdminsAtivos_contaApenasAdminsNaoExcluidos() {
        long baseline = usuarioRepository.contarAdminsAtivos();

        usuarioRepository.save(novo("admin1", admin));
        Usuario admin2 = novo("admin2", admin);
        admin2.setDataExclusao(Instant.now());
        admin2.setExcluidoPor("sistema");
        usuarioRepository.save(admin2);
        usuarioRepository.save(novo("comum", user));

        assertThat(usuarioRepository.contarAdminsAtivos()).isEqualTo(baseline + 1);
    }

    // ---------- C1 ----------

    @Test
    void listarParaGrid_trazUsuariosOrdenadosPorNome() {
        usuarioRepository.save(novo("zzz-zeca", user));
        usuarioRepository.save(novo("aaa-ana", user));

        var nomes = usuarioRepository.listarParaGrid().stream().map(Usuario::getNome).toList();

        assertThat(nomes).contains("aaa-ana", "zzz-zeca");
        assertThat(nomes.indexOf("aaa-ana")).isLessThan(nomes.indexOf("zzz-zeca"));
    }
}
