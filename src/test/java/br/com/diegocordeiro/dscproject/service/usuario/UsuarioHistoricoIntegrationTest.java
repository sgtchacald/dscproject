package br.com.diegocordeiro.dscproject.service.usuario;

import br.com.diegocordeiro.dscproject.dto.usuario.RevisaoUsuarioDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioDTO;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O histórico de usuário (Hibernate Envers) só é exercitado de verdade contra o
 * banco: a fatia de controller mocka o service. Este teste cobre o mapeamento
 * das revisões — a numeração de revisão do Envers é {@code int}, então o
 * repositório e o DTO usam {@code Integer}.
 */
@SpringBootTest
@ActiveProfiles({"dev", "test"})
class UsuarioHistoricoIntegrationTest {

    private static final String LOGIN = "historico-teste";
    private static final String EMAIL = "historico-teste@teste.local";

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;

    @BeforeEach
    @AfterEach
    void limpar() {
        Usuario existente = usuarioRepository.findByLoginOrEmail(LOGIN, EMAIL);
        if (existente != null) {
            usuarioRepository.delete(existente);
        }
    }

    @Test
    void buscarHistorico_aposCriarEEditar_devolveAsRevisoesSemErroDeCast() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNome("Nome Original");
        dto.setGenero(Genero.OUTRO);
        dto.setNascimento(LocalDate.of(1990, 1, 1));
        dto.setEmail(EMAIL);
        dto.setLogin(LOGIN);
        dto.setSenha("senhaInicial");
        dto.setConfirmacaoSenha("senhaInicial");
        dto.setPerfilCodigo("USER");
        Long id = usuarioService.inserir(dto).getId();

        dto.setNome("Nome Editado");
        usuarioService.editar(id, dto);

        Page<RevisaoUsuarioDTO> historico = usuarioService.buscarHistorico(id, PageRequest.of(0, 20));

        assertThat(historico.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(historico.getContent()).extracting(RevisaoUsuarioDTO::getTipo)
            .contains("Criação", "Alteração");
        assertThat(historico.getContent()).allSatisfy(revisao -> {
            assertThat(revisao.getRevisao()).isNotNull();
            assertThat(revisao.getLogin()).isEqualTo(LOGIN);
        });
    }
}
