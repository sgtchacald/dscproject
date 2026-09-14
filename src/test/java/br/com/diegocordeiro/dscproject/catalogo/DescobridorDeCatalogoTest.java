package br.com.diegocordeiro.dscproject.catalogo;

import br.com.diegocordeiro.dscproject.config.catalogo.DescobridorDeCatalogo;
import br.com.diegocordeiro.dscproject.config.parametro.ParametroDefinido;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosGlobaisCatalogo;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoDefinida;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoPerfilCatalogo;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoUsuarioCatalogo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescobridorDeCatalogoTest {

    private static final String PACOTE_PERMISSAO = "br.com.diegocordeiro.dscproject.config.permissao";
    private static final String PACOTE_PARAMETRO = "br.com.diegocordeiro.dscproject.config.parametro";

    @Test
    void noPacote_permissoes_trazTodasAsConstantesDeCadaEnumDoModulo() {
        List<PermissaoDefinida> descobertas =
            DescobridorDeCatalogo.noPacote(PACOTE_PERMISSAO, PermissaoDefinida.class);

        assertThat(descobertas)
            .contains(PermissaoUsuarioCatalogo.values())
            .contains(PermissaoPerfilCatalogo.values())
            .doesNotHaveDuplicates()
            .allMatch(item -> item.getClass().isEnum());
    }

    @Test
    void noPacote_parametros_trazTodasAsConstantesDeCadaEnumDoModulo() {
        List<ParametroDefinido> descobertos =
            DescobridorDeCatalogo.noPacote(PACOTE_PARAMETRO, ParametroDefinido.class);

        assertThat(descobertos)
            .contains(ParametrosGlobaisCatalogo.values())
            .doesNotHaveDuplicates()
            .allMatch(item -> item.getClass().isEnum());
    }

    @Test
    void noPacote_ignoraAInterface_naoConfundeOTipoDeCatalogoComUmItem() {
        List<PermissaoDefinida> descobertas =
            DescobridorDeCatalogo.noPacote(PACOTE_PERMISSAO, PermissaoDefinida.class);

        assertThat(descobertas).isNotEmpty();
        assertThat(descobertas).noneMatch(item -> item.getClass().isInterface());
    }
}
