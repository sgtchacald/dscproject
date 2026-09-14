package br.com.diegocordeiro.dscproject.config.parametro;

import br.com.diegocordeiro.dscproject.config.catalogo.ItemCatalogo;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoDefinida;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;

/**
 * Entrada do catálogo de parâmetros globais definido no código. Cada enum de
 * módulo implementa esta interface; o {@code ParametroCatalogoService} reflete
 * estas entradas na tabela {@code PARAMETROS_GLOBAIS}. Mesmo desenho do catálogo
 * de permissões ({@link PermissaoDefinida}).
 *
 * <p>Um parâmetro é um <b>contrato da aplicação</b>: o código lê
 * {@code buscarValorPorCodigo(codigo)} e espera a chave existir. Por isso a tela
 * não cria nem exclui parâmetro — só edita o valor.</p>
 */
public interface ParametroDefinido extends ItemCatalogo {

    TipoParametro getTipo();

    /** Valor semeado na primeira carga; base da ação "restaurar padrão". */
    String getValorDefault();
}
