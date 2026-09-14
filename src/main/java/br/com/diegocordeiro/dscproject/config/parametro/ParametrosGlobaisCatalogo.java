package br.com.diegocordeiro.dscproject.config.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;

/**
 * Catálogo, no código, dos parâmetros do próprio módulo Parâmetros Globais.
 * São os itens que condicionam o comportamento da própria tela de parâmetros.
 * A carga inicial reflete estas entradas na tabela {@code PARAMETROS_GLOBAIS}.
 */
public enum ParametrosGlobaisCatalogo implements ParametroDefinido {

    PARAMETROS_SYNC_CATALOGO_NA_INICIALIZACAO(
        "Sincronizar catálogo na inicialização",
        "Quando verdadeiro, o sincronizador do catálogo de parâmetros (código -> tabela) roda a cada subida da aplicação.",
        TipoParametro.BOOLEAN, "true"),

    PARAMETROS_VALOR_CACHE(
        "Cache do valor de parâmetro",
        "Quando verdadeiro, o valor lido por buscarValorPorCodigo pode ser cacheado em memória e invalidado nas gravações desta tela.",
        TipoParametro.BOOLEAN, "true");

    public static final String MODULO = "Parâmetros Globais";

    private final String nome;
    private final String descricao;
    private final TipoParametro tipo;
    private final String valorDefault;

    ParametrosGlobaisCatalogo(String nome, String descricao, TipoParametro tipo, String valorDefault) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.valorDefault = valorDefault;
    }

    @Override
    public String getCodigo() {
        return name();
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String getModulo() {
        return MODULO;
    }

    @Override
    public TipoParametro getTipo() {
        return tipo;
    }

    @Override
    public String getValorDefault() {
        return valorDefault;
    }
}
