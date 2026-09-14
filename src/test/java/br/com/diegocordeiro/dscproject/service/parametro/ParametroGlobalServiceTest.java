package br.com.diegocordeiro.dscproject.service.parametro;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroValorFormDTO;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametroGlobalServiceTest {

    @Mock
    private ParametroGlobalRepository parametroGlobalRepository;

    private ParametroGlobalService service(boolean orfaoEdicaoBloqueada) {
        ParametroGlobalService s = new ParametroGlobalService(parametroGlobalRepository, orfaoEdicaoBloqueada);
        lenient().when(parametroGlobalRepository.save(any(ParametroGlobal.class))).thenAnswer(i -> i.getArgument(0));
        return s;
    }

    private ParametroGlobal parametro(Long id, TipoParametro tipo, String valor, boolean orfa) {
        ParametroGlobal p = new ParametroGlobal();
        p.setId(id);
        p.setCodigo("CONTA_MOEDA_PADRAO");
        p.setNome("Moeda padrão da conta");
        p.setModulo("Contas");
        p.setTipoDado(tipo);
        p.setValor(valor);
        p.setValorDefault("BRL");
        p.setOrfa(orfa);
        return p;
    }

    private ParametroValorFormDTO form(String valor, String motivo) {
        ParametroValorFormDTO dto = new ParametroValorFormDTO();
        dto.setValor(valor);
        dto.setMotivo(motivo);
        return dto;
    }

    // ---------- BDD 16.2 — editar valor STRING ----------

    @Test
    void editarValor_stringValida_persisteValorEMotivo() {
        ParametroGlobal p = parametro(7L, TipoParametro.STRING, "BRL", false);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        service(false).editarValor(7L, form("USD", "Conta internacional"));

        assertThat(p.getValor()).isEqualTo("USD");
        assertThat(p.getMotivo()).isEqualTo("Conta internacional");
        verify(parametroGlobalRepository).save(p);
    }

    // ---------- BDD 16.4 — decimal aceita vírgula ----------

    @Test
    void editarValor_decimalComVirgula_persiste() {
        ParametroGlobal p = parametro(7L, TipoParametro.DECIMAL, "1.00", false);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        service(false).editarValor(7L, form("10,50", "ajuste"));

        assertThat(p.getValor()).isEqualTo("10,50");
    }

    // ---------- RN02 — valor inválido para o tipo ----------

    @Test
    void editarValor_inteiroInvalido_lancaRegraNegocioNoCampoValor() {
        ParametroGlobal p = parametro(7L, TipoParametro.INTEGER, "1", false);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service(false).editarValor(7L, form("abc", "motivo")))
            .isInstanceOf(RegraNegocioException.class)
            .satisfies(ex -> {
                assertThat(((RegraNegocioException) ex).getCampo()).isEqualTo("valor");
                assertThat(ex.getMessage()).isEqualTo("parametro.valor.inteiro.invalido");
            });
        verify(parametroGlobalRepository, never()).save(any());
    }

    // ---------- RN04 — só valor e motivo são gravados ----------

    @Test
    void editarValor_naoTocaCodigoNomeModuloTipo() {
        ParametroGlobal p = parametro(7L, TipoParametro.STRING, "BRL", false);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        service(false).editarValor(7L, form("USD", "motivo"));

        assertThat(p.getCodigo()).isEqualTo("CONTA_MOEDA_PADRAO");
        assertThat(p.getNome()).isEqualTo("Moeda padrão da conta");
        assertThat(p.getModulo()).isEqualTo("Contas");
        assertThat(p.getTipoDado()).isEqualTo(TipoParametro.STRING);
    }

    // ---------- RN06 / Seção 17 — trava de órfão configurável ----------

    @Test
    void editarValor_orfaoComTravaLigada_lancaRegraNegocio() {
        ParametroGlobal p = parametro(7L, TipoParametro.STRING, "BRL", true);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service(true).editarValor(7L, form("USD", "motivo")))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("parametro.orfao.nao.editavel");
        verify(parametroGlobalRepository, never()).save(any());
    }

    @Test
    void editarValor_orfaoComTravaDesligada_permiteAEdicao() {
        ParametroGlobal p = parametro(7L, TipoParametro.STRING, "BRL", true);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        service(false).editarValor(7L, form("USD", "motivo"));

        assertThat(p.getValor()).isEqualTo("USD");
    }

    // ---------- RN07 / BDD 16.13 — restaurar ao padrão ----------

    @Test
    void restaurarPadrao_gravaOValorDefaultEOMotivo() {
        ParametroGlobal p = parametro(7L, TipoParametro.STRING, "USD", false);
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(7L)).thenReturn(Optional.of(p));

        service(false).restaurarPadrao(7L, "voltando ao padrão");

        assertThat(p.getValor()).isEqualTo("BRL");
        assertThat(p.getMotivo()).isEqualTo("voltando ao padrão");
    }

    // ---------- não encontrado ----------

    @Test
    void editarValor_idInexistente_lancaRegistroNaoEncontrado() {
        when(parametroGlobalRepository.findByIdAndDataExclusaoIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service(false).editarValor(99L, form("x", "y")))
            .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    // ---------- C4 ----------

    @Test
    void buscarValorPorCodigo_devolveOValorCorrente() {
        ParametroGlobal p = parametro(7L, TipoParametro.STRING, "BRL", false);
        when(parametroGlobalRepository.findByCodigo("CONTA_MOEDA_PADRAO")).thenReturn(Optional.of(p));

        assertThat(service(false).buscarValorPorCodigo("CONTA_MOEDA_PADRAO")).isEqualTo("BRL");
    }
}
