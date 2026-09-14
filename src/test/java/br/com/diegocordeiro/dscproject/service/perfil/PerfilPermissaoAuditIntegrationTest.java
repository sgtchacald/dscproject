package br.com.diegocordeiro.dscproject.service.perfil;

import br.com.diegocordeiro.dscproject.dto.perfil.PerfilFormDTO;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilPermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A alteração do vínculo perfil x permissão só é exercitada de verdade contra o
 * banco. Cobre o rastro de auditoria da remoção do vínculo (quem / quando) e o
 * efeito na resolução de autoridades de um usuário do perfil.
 */
@SpringBootTest
@ActiveProfiles({"dev", "test"})
class PerfilPermissaoAuditIntegrationTest {

    @Autowired private PerfilService perfilService;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PerfilPermissaoRepository perfilPermissaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "auditoria-vinculo")
    void editar_removeVinculo_registraExcluidoPorEUsuarioPerdeAAutoridade() {
        String s = UUID.randomUUID().toString().substring(0, 8);
        Perfil perfil = perfilRepository.save(new Perfil(
            "AUD_" + s, "Auditoria " + s, null, false));
        Usuario usuario = usuarioRepository.save(usuario("aud_" + s, perfil));

        PerfilFormDTO comTresPermissoes = form("AUD_" + s, "Auditoria " + s,
            "PERFIS_VINCULAR_PERMISSAO", "USUARIOS_EDITAR", "USUARIOS_VER_HISTORICO");
        perfilService.editar(perfil.getId(), comTresPermissoes, null);

        PerfilFormDTO semHistorico = form("AUD_" + s, "Auditoria " + s,
            "PERFIS_VINCULAR_PERMISSAO", "USUARIOS_EDITAR");
        perfilService.editar(perfil.getId(), semHistorico, null);

        PerfilPermissao removido = perfilPermissaoRepository.findAll().stream()
            .filter(vinculo -> vinculo.getPerfil().getId().equals(perfil.getId()))
            .filter(vinculo -> vinculo.getPermissao().getCodigo().equals("USUARIOS_VER_HISTORICO"))
            .findFirst().orElseThrow();

        assertThat(removido.getDataExclusao()).isNotNull();
        assertThat(removido.getExcluidoPor()).isEqualTo("auditoria-vinculo");

        Usuario recarregado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        List<String> autoridades = recarregado.getAuthorities().stream()
            .map(Object::toString).toList();
        assertThat(autoridades).contains("PERM_PERFIS_VINCULAR_PERMISSAO", "PERM_USUARIOS_EDITAR");
        assertThat(autoridades).doesNotContain("PERM_USUARIOS_VER_HISTORICO");
    }

    private static PerfilFormDTO form(String codigo, String nome, String... permissoes) {
        PerfilFormDTO dto = new PerfilFormDTO();
        dto.setCodigo(codigo);
        dto.setNome(nome);
        dto.setPermissoes(new ArrayList<>(List.of(permissoes)));
        return dto;
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
}
