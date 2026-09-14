package br.com.diegocordeiro.dscproject.service.parametro;

import br.com.diegocordeiro.dscproject.dto.parametro.ParametroValorFormDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.RevisaoParametroDTO;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RN09 / BDD 16.9 e 16.14 — o histórico de um parâmetro (Hibernate Envers) só é
 * exercitado de verdade contra o banco. Cobre as revisões geradas por duas
 * edições, com autor, valor e motivo, da mais recente para a mais antiga.
 */
@SpringBootTest
@ActiveProfiles({"dev", "test"})
class ParametroHistoricoIntegrationTest {

    @Autowired
    private ParametroGlobalService parametroGlobalService;
    @Autowired
    private ParametroGlobalRepository parametroGlobalRepository;

    private String codigo;

    @BeforeEach
    void criarParametro() {
        codigo = "HIST_TESTE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ParametroGlobal p = new ParametroGlobal();
        p.setCodigo(codigo);
        p.setNome("Histórico de teste");
        p.setDescricao("Parâmetro de teste do histórico.");
        p.setModulo("Testes");
        p.setTipoDado(TipoParametro.STRING);
        p.setValor("v0");
        p.setValorDefault("v0");
        parametroGlobalRepository.saveAndFlush(p);
    }

    @AfterEach
    void limpar() {
        parametroGlobalRepository.findByCodigo(codigo).ifPresent(parametroGlobalRepository::delete);
    }

    @Test
    @WithMockUser(username = "editor-historico")
    void buscarHistorico_aposDuasEdicoes_devolveRevisoesDecrescentesComAutorValorEMotivo() {
        Long id = parametroGlobalRepository.findByCodigo(codigo).orElseThrow().getId();

        parametroGlobalService.editarValor(id, form("v1", "primeira alteração"));
        parametroGlobalService.editarValor(id, form("v2", "segunda alteração"));

        Page<RevisaoParametroDTO> historico = parametroGlobalService.buscarHistorico(id, PageRequest.of(0, 20));

        assertThat(historico.getContent()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(historico.getContent().get(0).getRevisao())
            .isGreaterThan(historico.getContent().get(1).getRevisao());
        assertThat(historico.getContent()).anySatisfy(r -> {
            assertThat(r.getValor()).isEqualTo("v2");
            assertThat(r.getMotivo()).isEqualTo("segunda alteração");
            assertThat(r.getAutor()).isEqualTo("editor-historico");
        });
    }

    private static ParametroValorFormDTO form(String valor, String motivo) {
        ParametroValorFormDTO dto = new ParametroValorFormDTO();
        dto.setValor(valor);
        dto.setMotivo(motivo);
        return dto;
    }
}
