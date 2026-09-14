package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Cartões de Crédito.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoCartaoCatalogo implements PermissaoDefinida {

    CARTOES_LISTAR("Listar meus cartões", "Abrir a tela Meus Cartões, listar e filtrar os próprios cartões. Controla a visibilidade do menu 'Cartões'."),
    CARTOES_INSERIR("Cadastrar cartões", "Cadastrar novos cartões de crédito."),
    CARTOES_EDITAR("Editar cartões", "Alterar dados cadastrais de cartões existentes."),
    CARTOES_EXCLUIR("Excluir cartões", "Excluir logicamente cartões que não possuem faturas ou despesas vinculadas."),
    CARTOES_DESATIVAR("Desativar cartões", "Desativar cartões ativos impedindo novos lançamentos."),
    CARTOES_ATIVAR("Ativar cartões", "Reativar cartões inativos permitindo novos lançamentos.");

    public static final String MODULO = "Cartões de Crédito";

    private final String nome;
    private final String descricao;

    PermissaoCartaoCatalogo(String nome, String descricao) {
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
