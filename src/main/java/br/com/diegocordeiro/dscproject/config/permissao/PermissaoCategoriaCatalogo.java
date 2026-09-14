package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Categorias.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoCategoriaCatalogo implements PermissaoDefinida {

    CATEGORIAS_LISTAR("Listar categorias", "Abrir a tela de Categorias, listar e filtrar. Controla a visibilidade do menu 'Categorias'."),
    CATEGORIAS_INSERIR("Cadastrar categoria", "Cadastrar nova categoria."),
    CATEGORIAS_EDITAR("Editar categoria", "Editar categoria existente, inclusive desativar/reativar."),
    CATEGORIAS_EXCLUIR("Excluir categoria", "Exclusão lógica de categoria, respeitadas as travas.");

    public static final String MODULO = "Categorias";

    private final String nome;
    private final String descricao;

    PermissaoCategoriaCatalogo(String nome, String descricao) {
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
