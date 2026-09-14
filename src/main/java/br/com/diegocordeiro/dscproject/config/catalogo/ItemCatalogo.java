package br.com.diegocordeiro.dscproject.config.catalogo;

/**
 * Entrada de um catálogo definido no código — a fonte da verdade que o
 * {@link SincronizadorDeCatalogo} reflete numa tabela. Cada {@code enum} de módulo
 * (permissões, parâmetros globais, …) implementa esta interface, direta ou por um
 * subtipo ({@code PermissaoDefinida}, {@code ParametroDefinido}).
 */
public interface ItemCatalogo {

    String getCodigo();

    String getNome();

    String getDescricao();

    String getModulo();
}
