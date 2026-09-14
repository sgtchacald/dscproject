package br.com.diegocordeiro.dscproject.config.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;

/**
 * Catálogo, no código, dos parâmetros do módulo Categorias.
 * A carga inicial reflete estas entradas na tabela {@code PARAMETROS_GLOBAIS}.
 */
public enum ParametrosCategoriaCatalogo implements ParametroDefinido {

    CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO(
        "Exclusão de categoria bloqueia em uso",
        "Se verdadeiro, impede excluir uma categoria referenciada por lançamentos (só permite desativar). Se falso, a exclusão é permitida e os lançamentos afetados têm a categoria anulada.",
        TipoParametro.BOOLEAN, "true"),

    CATEGORIA_COMBOBOX_CACHE(
        "Cache do combobox de categorias",
        "Quando verdadeiro, a lista de categorias ativas para novos lançamentos é cacheada em memória e invalidada nas alterações do catálogo.",
        TipoParametro.BOOLEAN, "true");

    public static final String MODULO = "Categorias";

    private final String nome;
    private final String descricao;
    private final TipoParametro tipo;
    private final String valorDefault;

    ParametrosCategoriaCatalogo(String nome, String descricao, TipoParametro tipo, String valorDefault) {
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
