package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Instituições por Provedor.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoInstituicaoProvedorCatalogo implements PermissaoDefinida {

    INSTITUICOES_PROVEDOR_LISTAR("Listar instituições por provedor", "Abrir a tela de Instituições por Provedor, listar e filtrar. Controla a visibilidade do menu 'Instituições por Provedor'."),
    INSTITUICOES_PROVEDOR_INSERIR("Cadastrar instituições por provedor", "Cadastrar o vínculo instituição × provedor."),
    INSTITUICOES_PROVEDOR_EDITAR("Editar instituições por provedor", "Alterar o vínculo instituição × provedor."),
    INSTITUICOES_PROVEDOR_EXCLUIR("Excluir instituições por provedor", "Excluir o vínculo instituição × provedor.");

    public static final String MODULO = "Instituições por Provedor";

    private final String nome;
    private final String descricao;

    PermissaoInstituicaoProvedorCatalogo(String nome, String descricao) {
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
