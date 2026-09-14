package br.com.diegocordeiro.dscproject.config.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;

/**
 * Catálogo, no código, dos parâmetros do módulo Instituições Financeiras.
 * A carga inicial reflete estas entradas na tabela {@code PARAMETROS_GLOBAIS}.
 */
public enum ParametrosInstituicaoCatalogo implements ParametroDefinido {

    INSTITUICAO_EXCLUSAO_BLOQUEIA_EM_USO(
        "Exclusão de instituição bloqueia em uso",
        "Se verdadeiro, impede excluir uma instituição referenciada por contas, investimentos ou conexões de Open Finance (só desativar). Se falso, anula as FKs nos registros que o permitirem.",
        TipoParametro.BOOLEAN, "true"),

    INSTITUICAO_COMBOBOX_CACHE(
        "Cache do combobox de instituições",
        "Quando verdadeiro, a lista de instituições ativas é cacheada em memória e invalidada nas alterações do catálogo.",
        TipoParametro.BOOLEAN, "true");

    public static final String MODULO = "Instituições Financeiras";

    private final String nome;
    private final String descricao;
    private final TipoParametro tipo;
    private final String valorDefault;

    ParametrosInstituicaoCatalogo(String nome, String descricao, TipoParametro tipo, String valorDefault) {
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
