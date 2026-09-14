package br.com.diegocordeiro.dscproject.repository.parametro;

import br.com.diegocordeiro.dscproject.config.JpaAuditingConfig;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"dev", "test"})
@Import(JpaAuditingConfig.class)
class ParametroGlobalRepositoryTest {

    @Autowired
    private ParametroGlobalRepository parametroGlobalRepository;

    private String sufixo() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ParametroGlobal parametro(String codigo, String modulo, String nome) {
        ParametroGlobal p = new ParametroGlobal();
        p.setCodigo(codigo);
        p.setNome(nome);
        p.setDescricao(nome);
        p.setModulo(modulo);
        p.setTipoDado(TipoParametro.STRING);
        p.setValor("v");
        p.setValorDefault("v");
        return p;
    }

    // ---------- C1 ----------

    @Test
    void findByDataExclusaoIsNullOrderByModuloAscNomeAsc_ordenaPorModuloDepoisNome() {
        String s = sufixo();
        parametroGlobalRepository.save(parametro("Z_" + s, "MOD_Z_" + s, "Zeta " + s));
        parametroGlobalRepository.save(parametro("A_" + s, "MOD_Z_" + s, "Alfa " + s));

        List<String> nomes = parametroGlobalRepository.findByDataExclusaoIsNullOrderByModuloAscNomeAsc().stream()
            .filter(p -> ("MOD_Z_" + s).equals(p.getModulo()))
            .map(ParametroGlobal::getNome)
            .toList();

        assertThat(nomes).containsExactly("Alfa " + s, "Zeta " + s);
    }

    @Test
    void findByDataExclusaoIsNullOrderByModuloAscNomeAsc_naoTrazExcluido() {
        String s = sufixo();
        ParametroGlobal excluido = parametro("EXC_" + s, "MOD_" + s, "Excluído " + s);
        excluido.setDataExclusao(Instant.now());
        excluido.setExcluidoPor("sistema");
        parametroGlobalRepository.save(excluido);

        assertThat(parametroGlobalRepository.findByDataExclusaoIsNullOrderByModuloAscNomeAsc())
            .noneMatch(p -> ("EXC_" + s).equals(p.getCodigo()));
    }

    // ---------- C3 / C4 ----------

    @Test
    void findByCodigo_encontraPelaChaveEstavel() {
        String s = sufixo();
        parametroGlobalRepository.save(parametro("CHAVE_" + s, "MOD_" + s, "Chave " + s));

        assertThat(parametroGlobalRepository.findByCodigo("CHAVE_" + s)).isPresent();
        assertThat(parametroGlobalRepository.findByCodigo("NAO_EXISTE_" + s)).isEmpty();
    }
}
