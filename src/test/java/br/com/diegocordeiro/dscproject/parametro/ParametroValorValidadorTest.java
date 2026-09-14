package br.com.diegocordeiro.dscproject.parametro;

import br.com.diegocordeiro.dscproject.config.parametro.ParametroValorValidador;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RN02 / RT06 — validação do valor conforme o tipo. Cobre o defeito lógico da
 * checagem de BOOLEAN da geração 1 (não pode voltar a aceitar "sim").
 */
class ParametroValorValidadorTest {

    // ---------- STRING ----------

    @Test
    void string_qualquerTextoNaoVazio_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.STRING, "qualquer coisa")).isEmpty();
    }

    @Test
    void string_vazio_erroDeObrigatorio() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.STRING, "   "))
            .contains("parametro.valor.obrigatorio");
    }

    // ---------- INTEGER — MSG03 ----------

    @Test
    void integer_numeroInteiro_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.INTEGER, "42")).isEmpty();
    }

    @Test
    void integer_textoNaoNumerico_MSG03() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.INTEGER, "abc"))
            .contains("parametro.valor.inteiro.invalido");
    }

    @Test
    void integer_decimal_MSG03() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.INTEGER, "10.5"))
            .contains("parametro.valor.inteiro.invalido");
    }

    // ---------- DECIMAL — MSG04, aceita vírgula ----------

    @Test
    void decimal_comPonto_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.DECIMAL, "10.50")).isEmpty();
    }

    @Test
    void decimal_comVirgula_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.DECIMAL, "10,50")).isEmpty();
    }

    @Test
    void decimal_textoInvalido_MSG04() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.DECIMAL, "dez"))
            .contains("parametro.valor.decimal.invalido");
    }

    // ---------- BOOLEAN — MSG05, sem o bug !a || !b ----------

    @Test
    void boolean_true_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.BOOLEAN, "true")).isEmpty();
    }

    @Test
    void boolean_falseMaiusculo_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.BOOLEAN, "FALSE")).isEmpty();
    }

    @Test
    void boolean_valorSim_MSG05() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.BOOLEAN, "sim"))
            .contains("parametro.valor.booleano.invalido");
    }

    @Test
    void boolean_valorNumerico_MSG05() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.BOOLEAN, "1"))
            .contains("parametro.valor.booleano.invalido");
    }

    // ---------- JSON — MSG06 ----------

    @Test
    void json_objetoValido_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.JSON, "{\"chave\": 1}")).isEmpty();
    }

    @Test
    void json_arrayValido_valido() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.JSON, "[1, 2, 3]")).isEmpty();
    }

    @Test
    void json_sintaxeQuebrada_MSG06() {
        assertThat(ParametroValorValidador.chaveErro(TipoParametro.JSON, "{\"chave\": }"))
            .contains("parametro.valor.json.invalido");
    }

    // ---------- Tipo desconhecido — MSG07 ----------

    @Test
    void tipoNulo_MSG07() {
        assertThat(ParametroValorValidador.chaveErro(null, "x"))
            .contains("parametro.tipo.desconhecido");
    }
}
