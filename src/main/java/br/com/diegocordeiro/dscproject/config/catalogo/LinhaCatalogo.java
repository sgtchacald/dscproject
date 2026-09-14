package br.com.diegocordeiro.dscproject.config.catalogo;

/**
 * Linha da tabela que espelha um catálogo do código ({@code PERMISSOES},
 * {@code PARAMETROS_GLOBAIS}, …). O {@link SincronizadorDeCatalogo} usa só o
 * código, para casar com o item do código, e a flag de órfã — nunca apaga linha.
 */
public interface LinhaCatalogo {

    String getCodigo();

    boolean isOrfa();

    void setOrfa(boolean orfa);
}
