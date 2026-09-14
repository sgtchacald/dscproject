package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Categorias por Provedor.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoCategoriaProvedorCatalogo implements PermissaoDefinida {

    CATEGORIAS_PROVEDOR_LISTAR("Listar categorias por provedor", "Abrir a tela de Categorias por Provedor, listar e filtrar. Controla a visibilidade do menu 'Categorias por Provedor'."),
    CATEGORIAS_PROVEDOR_INSERIR("Cadastrar vínculo de categoria por provedor", "Cadastrar novo vínculo categoria × provedor."),
    CATEGORIAS_PROVEDOR_EDITAR("Editar vínculo de categoria por provedor", "Editar vínculo categoria × provedor existente."),
    CATEGORIAS_PROVEDOR_EXCLUIR("Excluir vínculo de categoria por provedor", "Exclusão lógica de vínculo categoria × provedor.");

    public static final String MODULO = "Categorias por Provedor";

    private final String nome;
    private final String descricao;

    PermissaoCategoriaProvedorCatalogo(String nome, String descricao) {
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
