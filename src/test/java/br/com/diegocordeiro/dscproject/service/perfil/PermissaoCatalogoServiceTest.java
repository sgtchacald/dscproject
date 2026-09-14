package br.com.diegocordeiro.dscproject.service.perfil;

import br.com.diegocordeiro.dscproject.config.catalogo.DescobridorDeCatalogo;
import br.com.diegocordeiro.dscproject.dto.catalogo.SincronizacaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoDefinida;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.repository.perfil.PermissaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissaoCatalogoServiceTest {

    @Mock
    private PermissaoRepository permissaoRepository;

    @InjectMocks
    private PermissaoCatalogoService service;

    private static List<PermissaoDefinida> permissoesDoCodigo() {
        return DescobridorDeCatalogo.noPacote("br.com.diegocordeiro.dscproject.config.permissao", PermissaoDefinida.class);
    }

    private static Permissao permissao(String codigo, String modulo, boolean orfa) {
        Permissao p = new Permissao(codigo, codigo, null, modulo);
        p.setOrfa(orfa);
        return p;
    }

    // ---------- RN08 — insere as que faltam ----------

    @Test
    void sincronizar_tabelaVazia_insereTodasAsPermissoesDoCodigo() {
        when(permissaoRepository.findAll()).thenReturn(new ArrayList<>());

        SincronizacaoCatalogoDTO resultado = service.sincronizar();

        int totalCodigo = permissoesDoCodigo().size();
        assertThat(resultado.inseridas()).isEqualTo(totalCodigo);
        assertThat(resultado.orfas()).isZero();
        verify(permissaoRepository, org.mockito.Mockito.times(totalCodigo)).save(any(Permissao.class));
    }

    // ---------- BDD 16.8 — permissão nova aparece só no código ----------

    @Test
    void sincronizar_permissaoNovaNoCodigo_eInserida() {
        List<Permissao> tabela = new ArrayList<>();
        permissoesDoCodigo().forEach(d -> {
            if (!d.getCodigo().equals("PERFIS_SINCRONIZAR_CATALOGO")) {
                tabela.add(permissao(d.getCodigo(), d.getModulo(), false));
            }
        });
        when(permissaoRepository.findAll()).thenReturn(tabela);

        SincronizacaoCatalogoDTO resultado = service.sincronizar();

        assertThat(resultado.inseridas()).isEqualTo(1);
        assertThat(resultado.orfas()).isZero();
    }

    // ---------- RN09 / BDD 16.9 — permissão órfã ----------

    @Test
    void sincronizar_permissaoSoNaTabela_ficaMarcadaComoOrfa() {
        List<Permissao> tabela = new ArrayList<>();
        permissoesDoCodigo().forEach(d -> tabela.add(permissao(d.getCodigo(), d.getModulo(), false)));
        Permissao antiga = permissao("RECURSO_ANTIGO", "Usuários", false);
        tabela.add(antiga);
        when(permissaoRepository.findAll()).thenReturn(tabela);

        SincronizacaoCatalogoDTO resultado = service.sincronizar();

        assertThat(antiga.isOrfa()).isTrue();
        assertThat(resultado.orfas()).isEqualTo(1);
        assertThat(resultado.inseridas()).isZero();
    }

    @Test
    void sincronizar_permissaoOrfaQueVoltouAoCodigo_temAFlagDesmarcada() {
        List<Permissao> tabela = new ArrayList<>();
        Permissao voltou = permissao("PERFIS_LISTAR", "Perfis e Permissões", true);
        tabela.add(voltou);
        permissoesDoCodigo().forEach(d -> {
            if (!d.getCodigo().equals("PERFIS_LISTAR")) {
                tabela.add(permissao(d.getCodigo(), d.getModulo(), false));
            }
        });
        when(permissaoRepository.findAll()).thenReturn(tabela);

        service.sincronizar();

        assertThat(voltou.isOrfa()).isFalse();
    }

    @Test
    void sincronizar_nuncaApagaLinhaDePermissoes() {
        List<Permissao> tabela = new ArrayList<>();
        tabela.add(permissao("RECURSO_ANTIGO", "Usuários", false));
        when(permissaoRepository.findAll()).thenReturn(tabela);

        service.sincronizar();

        verify(permissaoRepository, never()).delete(any());
        verify(permissaoRepository, never()).deleteById(any());
    }
}
