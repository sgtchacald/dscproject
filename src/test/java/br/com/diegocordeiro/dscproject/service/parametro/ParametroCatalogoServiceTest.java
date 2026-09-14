package br.com.diegocordeiro.dscproject.service.parametro;

import br.com.diegocordeiro.dscproject.config.catalogo.DescobridorDeCatalogo;
import br.com.diegocordeiro.dscproject.dto.catalogo.SincronizacaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.config.parametro.ParametroDefinido;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametroCatalogoServiceTest {

    @Mock
    private ParametroGlobalRepository parametroGlobalRepository;

    @InjectMocks
    private ParametroCatalogoService service;

    private static List<ParametroDefinido> parametrosDoCodigo() {
        return DescobridorDeCatalogo.noPacote("br.com.diegocordeiro.dscproject.config.parametro", ParametroDefinido.class);
    }

    private static ParametroGlobal parametro(String codigo, String valor, boolean orfa) {
        ParametroGlobal p = new ParametroGlobal();
        p.setCodigo(codigo);
        p.setNome(codigo);
        p.setDescricao(codigo);
        p.setModulo("Parâmetros Globais");
        p.setTipoDado(TipoParametro.STRING);
        p.setValor(valor);
        p.setValorDefault(valor);
        p.setOrfa(orfa);
        return p;
    }

    private static ParametroGlobal doCatalogo(ParametroDefinido d, String valor) {
        ParametroGlobal p = new ParametroGlobal(d);
        p.setValor(valor);
        return p;
    }

    // ---------- RN05 — insere os que faltam ----------

    @Test
    void sincronizar_tabelaVazia_insereTodosOsParametrosDoCodigo() {
        when(parametroGlobalRepository.findAll()).thenReturn(new ArrayList<>());

        SincronizacaoCatalogoDTO resultado = service.sincronizar();

        int totalCodigo = parametrosDoCodigo().size();
        assertThat(resultado.inseridas()).isEqualTo(totalCodigo);
        assertThat(resultado.orfas()).isZero();
        verify(parametroGlobalRepository, Mockito.times(totalCodigo)).save(any(ParametroGlobal.class));
    }

    // ---------- BDD 16.11 — parâmetro novo só no código ----------

    @Test
    void sincronizar_parametroNovoNoCodigo_eInserido() {
        List<ParametroDefinido> catalogo = parametrosDoCodigo();
        List<ParametroGlobal> tabela = new ArrayList<>();
        for (int i = 1; i < catalogo.size(); i++) {
            tabela.add(doCatalogo(catalogo.get(i), catalogo.get(i).getValorDefault()));
        }
        when(parametroGlobalRepository.findAll()).thenReturn(tabela);

        SincronizacaoCatalogoDTO resultado = service.sincronizar();

        assertThat(resultado.inseridas()).isEqualTo(1);
        assertThat(resultado.orfas()).isZero();
    }

    @Test
    void sincronizar_naoSobrescreveOValorDeParametroExistente() {
        ParametroDefinido primeiro = parametrosDoCodigo().get(0);
        ParametroGlobal existente = doCatalogo(primeiro, "valor-do-operador");
        when(parametroGlobalRepository.findAll()).thenReturn(new ArrayList<>(List.of(existente)));

        service.sincronizar();

        assertThat(existente.getValor()).isEqualTo("valor-do-operador");
        assertThat(existente.getValorDefault()).isEqualTo(primeiro.getValorDefault());
    }

    // ---------- RN06 / BDD 16.12 — parâmetro órfão ----------

    @Test
    void sincronizar_parametroSoNaTabela_ficaMarcadoComoOrfao() {
        List<ParametroGlobal> tabela = new ArrayList<>();
        parametrosDoCodigo().forEach(d -> tabela.add(doCatalogo(d, d.getValorDefault())));
        ParametroGlobal antigo = parametro("RECURSO_ANTIGO", "x", false);
        tabela.add(antigo);
        when(parametroGlobalRepository.findAll()).thenReturn(tabela);

        SincronizacaoCatalogoDTO resultado = service.sincronizar();

        assertThat(antigo.isOrfa()).isTrue();
        assertThat(resultado.orfas()).isEqualTo(1);
        assertThat(resultado.inseridas()).isZero();
    }

    @Test
    void sincronizar_parametroOrfaoQueVoltouAoCodigo_temAFlagDesmarcada() {
        List<ParametroDefinido> catalogo = parametrosDoCodigo();
        List<ParametroGlobal> tabela = new ArrayList<>();
        ParametroGlobal voltou = doCatalogo(catalogo.get(0), catalogo.get(0).getValorDefault());
        voltou.setOrfa(true);
        tabela.add(voltou);
        for (int i = 1; i < catalogo.size(); i++) {
            tabela.add(doCatalogo(catalogo.get(i), catalogo.get(i).getValorDefault()));
        }
        when(parametroGlobalRepository.findAll()).thenReturn(tabela);

        service.sincronizar();

        assertThat(voltou.isOrfa()).isFalse();
    }

    @Test
    void sincronizar_nuncaApagaLinha() {
        List<ParametroGlobal> tabela = new ArrayList<>();
        tabela.add(parametro("RECURSO_ANTIGO", "x", false));
        when(parametroGlobalRepository.findAll()).thenReturn(tabela);

        service.sincronizar();

        verify(parametroGlobalRepository, never()).delete(any());
        verify(parametroGlobalRepository, never()).deleteById(any());
    }
}
