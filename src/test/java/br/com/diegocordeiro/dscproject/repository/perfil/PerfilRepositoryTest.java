package br.com.diegocordeiro.dscproject.repository.perfil;

import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.config.JpaAuditingConfig;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilResumoDTO;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"dev", "test"})
@Import(JpaAuditingConfig.class)
class PerfilRepositoryTest {

    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PerfilPermissaoRepository perfilPermissaoRepository;
    @Autowired private PermissaoRepository permissaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private String sufixo() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Usuario usuario(String login, Perfil perfil) {
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

    // ---------- C1 ----------

    @Test
    void listarResumo_trazContagemDePermissoesAtivasEUsuariosAtivos() {
        String s = sufixo();
        Perfil perfil = perfilRepository.save(new Perfil("REL_" + s, "Relatórios " + s, null, false));
        Permissao p1 = permissaoRepository.save(new Permissao("PA_" + s, "Pa", null, "Mod"));
        Permissao p2 = permissaoRepository.save(new Permissao("PB_" + s, "Pb", null, "Mod"));
        perfilPermissaoRepository.save(new PerfilPermissao(perfil, p1));
        PerfilPermissao removido = new PerfilPermissao(perfil, p2);
        removido.setDataExclusao(Instant.now());
        removido.setExcluidoPor("sistema");
        perfilPermissaoRepository.save(removido);
        usuarioRepository.save(usuario("u1_" + s, perfil));
        Usuario excluido = usuario("u2_" + s, perfil);
        excluido.setDataExclusao(Instant.now());
        excluido.setExcluidoPor("sistema");
        usuarioRepository.save(excluido);

        PerfilResumoDTO linha = perfilRepository.listarResumo().stream()
            .filter(dto -> dto.getId().equals(perfil.getId()))
            .findFirst().orElseThrow();

        assertThat(linha.getQtdPermissoes()).isEqualTo(1);
        assertThat(linha.getQtdUsuarios()).isEqualTo(1);
        assertThat(linha.isSistema()).isFalse();
    }

    @Test
    void listarResumo_naoTrazPerfilExcluido() {
        String s = sufixo();
        Perfil perfil = new Perfil("EXC_" + s, "Excluído " + s, null, false);
        perfil.setDataExclusao(Instant.now());
        perfil.setExcluidoPor("sistema");
        perfilRepository.save(perfil);

        assertThat(perfilRepository.listarResumo()).noneMatch(dto -> dto.getId().equals(perfil.getId()));
    }

    // ---------- C4 / RN03 ----------

    @Test
    void contarPorCodigo_ignoraExcluidoEOProprioRegistro() {
        String s = sufixo();
        Perfil perfil = perfilRepository.save(new Perfil("DUP_" + s, "Dup " + s, null, false));

        assertThat(perfilRepository.contarPorCodigo("DUP_" + s, null)).isEqualTo(1L);
        assertThat(perfilRepository.contarPorCodigo("DUP_" + s, perfil.getId())).isZero();
        assertThat(perfilRepository.contarPorCodigo("NAO_EXISTE_" + s, null)).isZero();
    }

    // ---------- C5 / RN06 ----------

    @Test
    void contarPerfisComUsuarioAtivoConcedendo_contaSoComVinculoEUsuarioAtivos() {
        String s = sufixo();
        String codigoPermissao = "GERIR_" + s;
        Perfil perfil = perfilRepository.save(new Perfil("GEST_" + s, "Gestor " + s, null, false));
        Permissao permissao = permissaoRepository.save(new Permissao(codigoPermissao, "Gerir", null, "Mod"));
        perfilPermissaoRepository.save(new PerfilPermissao(perfil, permissao));

        assertThat(perfilRepository.contarPerfisComUsuarioAtivoConcedendo(codigoPermissao)).isZero();

        usuarioRepository.save(usuario("gestor_" + s, perfil));

        assertThat(perfilRepository.contarPerfisComUsuarioAtivoConcedendo(codigoPermissao)).isEqualTo(1L);
    }

    // ---------- C2 ----------

    @Test
    void buscarCodigosPermissaoAtivos_traSoOsVinculosNaoExcluidos() {
        String s = sufixo();
        Perfil perfil = perfilRepository.save(new Perfil("P_" + s, "P " + s, null, false));
        Permissao ativa = permissaoRepository.save(new Permissao("AT_" + s, "At", null, "Mod"));
        Permissao removida = permissaoRepository.save(new Permissao("RM_" + s, "Rm", null, "Mod"));
        perfilPermissaoRepository.save(new PerfilPermissao(perfil, ativa));
        PerfilPermissao vinculoRemovido = new PerfilPermissao(perfil, removida);
        vinculoRemovido.setDataExclusao(Instant.now());
        vinculoRemovido.setExcluidoPor("sistema");
        perfilPermissaoRepository.save(vinculoRemovido);

        assertThat(perfilPermissaoRepository.buscarCodigosPermissaoAtivos(perfil.getId()))
            .containsExactly("AT_" + s);
    }

    // ---------- C6 / RN07 ----------

    @Test
    void existsByPerfil_verdadeiroAteParaUsuarioExcluido() {
        String s = sufixo();
        Perfil perfil = perfilRepository.save(new Perfil("CU_" + s, "Cu " + s, null, false));
        Usuario excluido = usuario("cu_" + s, perfil);
        excluido.setDataExclusao(Instant.now());
        excluido.setExcluidoPor("sistema");
        usuarioRepository.save(excluido);

        assertThat(usuarioRepository.existsByPerfil(perfil)).isTrue();
    }

    // ---------- C3 ----------

    @Test
    void findByDataExclusaoIsNullOrderByModuloAscNomeAsc_ordenaPorModuloDepoisNome() {
        String s = sufixo();
        permissaoRepository.save(new Permissao("Z1_" + s, "Zeta " + s, null, "ZZ_" + s));
        permissaoRepository.save(new Permissao("A1_" + s, "Alfa " + s, null, "ZZ_" + s));

        var doModulo = permissaoRepository.findByDataExclusaoIsNullOrderByModuloAscNomeAsc().stream()
            .filter(p -> ("ZZ_" + s).equals(p.getModulo()))
            .map(Permissao::getNome)
            .toList();

        assertThat(doModulo).containsExactly("Alfa " + s, "Zeta " + s);
    }
}
