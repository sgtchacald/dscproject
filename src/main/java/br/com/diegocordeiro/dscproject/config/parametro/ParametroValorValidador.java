package br.com.diegocordeiro.dscproject.config.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Valida o valor de um parâmetro global conforme o seu {@link TipoParametro}.
 * Regra portada do {@code ParametroGlobalValidator} da geração 1, com o
 * {@code JSON} acrescentado e o defeito lógico da checagem de {@code BOOLEAN}
 * corrigido (a geração 1 usava {@code !isTrue || !isFalse}, sempre verdadeiro).
 *
 * <p>Sem estado e sem dependência de Spring — devolve a chave de i18n do erro,
 * ou vazio quando o valor é aceitável para o tipo.</p>
 */
public final class ParametroValorValidador {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private ParametroValorValidador() {
    }

    /** Chave de mensagem do erro, ou vazio se o valor é válido para o tipo. */
    public static Optional<String> chaveErro(TipoParametro tipo, String valor) {
        if (tipo == null) {
            return Optional.of("parametro.tipo.desconhecido");
        }
        if (valor == null || valor.isBlank()) {
            return Optional.of("parametro.valor.obrigatorio");
        }
        String v = valor.trim();
        return switch (tipo) {
            case STRING -> Optional.empty();
            case INTEGER -> ehInteiro(v) ? Optional.empty() : Optional.of("parametro.valor.inteiro.invalido");
            case DECIMAL -> ehDecimal(v) ? Optional.empty() : Optional.of("parametro.valor.decimal.invalido");
            case BOOLEAN -> ehBooleano(v) ? Optional.empty() : Optional.of("parametro.valor.booleano.invalido");
            case JSON -> ehJson(v) ? Optional.empty() : Optional.of("parametro.valor.json.invalido");
        };
    }

    private static boolean ehInteiro(String v) {
        try {
            Integer.parseInt(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean ehDecimal(String v) {
        try {
            new BigDecimal(v.replace(",", "."));   // aceita ',' como separador decimal
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean ehBooleano(String v) {
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false");
    }

    private static boolean ehJson(String v) {
        try {
            JSON_MAPPER.readTree(v);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
