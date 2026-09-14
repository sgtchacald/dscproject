package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Parâmetros Globais.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 * Não há {@code PARAMETROS_MANTER} — a tela não cria nem exclui parâmetro.
 */
public enum PermissaoParametroCatalogo implements PermissaoDefinida {

    PARAMETROS_LISTAR("Listar parâmetros globais", "Abrir a tela de Parâmetros Globais, listar, filtrar e ver o histórico de um parâmetro. Controla a visibilidade do menu 'Parâmetros Globais'."),
    PARAMETROS_EDITAR("Editar parâmetro global", "Editar o valor de um parâmetro (com motivo obrigatório) e restaurar o valor ao padrão.");

    public static final String MODULO = "Parâmetros Globais";

    private final String nome;
    private final String descricao;

    PermissaoParametroCatalogo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
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
}
