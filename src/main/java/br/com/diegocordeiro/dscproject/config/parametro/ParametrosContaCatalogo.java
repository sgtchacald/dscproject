package br.com.diegocordeiro.dscproject.config.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;

/**
 * Catálogo, no código, dos parâmetros do módulo Contas.
 * A carga inicial reflete estas entradas na tabela {@code PARAMETROS_GLOBAIS}.
 */
public enum ParametrosContaCatalogo implements ParametroDefinido {

    CONTA_EXCLUSAO_BLOQUEIA_EM_USO(
        "Exclusão de conta bloqueia em uso",
        "Se verdadeiro, impede excluir uma conta referenciada por lançamentos (só desativar). Se falso, a exclusão é permitida e os lançamentos com FK nullable ficam com CTA_ID nulo.",
        TipoParametro.BOOLEAN, "true"),

    CONTA_MOEDA_PADRAO(
        "Moeda padrão de conta",
        "Moeda pré-selecionada no cadastro de nova conta.",
        TipoParametro.STRING, "BRL"),

    CONTA_COMBOBOX_CACHE(
        "Cache do combobox de contas",
        "Se verdadeiro, a lista de contas ativas é cacheada por usuário e invalidada nas gravações de contas.",
        TipoParametro.BOOLEAN, "true");

    public static final String MODULO = "Contas";

    private final String nome;
    private final String descricao;
    private final TipoParametro tipo;
    private final String valorDefault;

    ParametrosContaCatalogo(String nome, String descricao, TipoParametro tipo, String valorDefault) {
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
